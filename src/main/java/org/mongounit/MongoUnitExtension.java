package org.mongounit;

import com.mongodb.client.MongoDatabase;
import java.util.List;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.mongounit.config.MongoUnitConfigurationUtil;
import org.mongounit.config.MongoUnitProperties;
import org.mongounit.model.AssertionResult;
import org.mongounit.model.MongoUnitCollection;
import org.mongounit.model.MongoUnitDatasets;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * {@link MongoUnitExtension} class is a JUnit 5 extension which enables the MongoUnit framework
 * features in a test target.
 */
public class MongoUnitExtension implements
    BeforeAllCallback,
    BeforeEachCallback,
    AfterEachCallback,
    AfterAllCallback {

  /**
   * Reference to the current Mongo database. This is for sharing with the {@link MongoUnit} static
   * methods only and not a substitute for the {@link Store}-based sharing model.
   */
  public static MongoDatabase CURRENT_MONGO_DATABASE;

  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(MongoUnitExtension.class);

  /**
   * Top namespace for this extension.
   */
  private static final Namespace NAMESPACE = Namespace.create(MongoUnitExtension.class);

  /**
   * Key with which to store {@link MongoDatabase} instance in the context store.
   */
  private static final String MONGODB_STORE_KEY = "mongoDatabase";

  /**
   * Key with which to store {@link MongoUnitProperties} instance in the context store.
   */
  private static final String MONGO_UNIT_PROPERTIES_KEY = "mongoUnitProperties";

  /**
   * Key with which to store class-level {@link MongoUnitDatasets}.
   */
  private static final String CLASS_MONGO_UNIT_DATASETS = "classMongoUnitDatasets";

  /**
   * Key with which to store method-level {@link MongoUnitDatasets}.
   */
  private static final String METHOD_MONGO_UNIT_DATASETS = "methodMongoUnitDatasets";

  @Override
  public void beforeAll(ExtensionContext context) {

    // Retrieve database instance through Spring context
    ApplicationContext springContext = SpringExtension.getApplicationContext(context);
    MongoDbFactory mongoDbFactory = springContext.getBean(MongoDbFactory.class);
    MongoDatabase mongoDatabase = mongoDbFactory.getDb();

    // Cache the database for this run in case manual seeding and assertion is done
    CURRENT_MONGO_DATABASE = mongoDatabase;

    // Store mongo database instance in the class namespace store
    Store extensionStore = getExtensionStore(context);
    extensionStore.put(MONGODB_STORE_KEY, mongoDatabase);

    // Load MongoUnitProperties and save them in extension store
    MongoUnitProperties mongoUnitProperties = MongoUnitConfigurationUtil.loadMongoUnitProperties();
    extensionStore.put(MONGO_UNIT_PROPERTIES_KEY, mongoUnitProperties);

    // Extract class-level datasets based on MongoUnit annotations
    MongoUnitDatasets mongoUnitDatasets = MongoUnitUtil.extractMongoUnitDatasets(context, true);

    // Save class level datasets in the store
    extensionStore.put(CLASS_MONGO_UNIT_DATASETS, mongoUnitDatasets);
  }

  @Override
  public void beforeEach(ExtensionContext context) {

    // Retrieve mongoDatabase and mongo unit properties from the extension namespace store
    Store extensionStore = getExtensionStore(context);
    MongoDatabase mongoDatabase = extensionStore.get(MONGODB_STORE_KEY, MongoDatabase.class);
    MongoUnitProperties mongoUnitProperties =
        extensionStore.get(MONGO_UNIT_PROPERTIES_KEY, MongoUnitProperties.class);

    // Clear all collections out of the database
    MongoUnitUtil.dropAllCollectionsInDatabase(mongoDatabase);

    // Retrieve class-level datasets from store
    MongoUnitDatasets classLevelMongoUnitDatasets =
        extensionStore.get(CLASS_MONGO_UNIT_DATASETS, MongoUnitDatasets.class);

    // Extract method-level datasets based on MongoUnit annotations
    MongoUnitDatasets methodLevelMongoUnitDatasets = MongoUnitUtil
        .extractMongoUnitDatasets(context, false);

    // Get method-level store and save method level dataset
    Store methodStore = getMethodStore(context);
    methodStore.put(METHOD_MONGO_UNIT_DATASETS, methodLevelMongoUnitDatasets);

    // Combine class and method seed datasets
    List<MongoUnitCollection> combinedDataset =
        MongoUnitUtil.combineDatasets(
            classLevelMongoUnitDatasets.getSeedWithDatasets(),
            methodLevelMongoUnitDatasets.getSeedWithDatasets());

    // Seed database with this dataset
    try {

      MongoUnitUtil.toDatabase(combinedDataset, mongoDatabase, mongoUnitProperties);

    } catch (MongoUnitException mongoUnitException) {

      // Log error and rethrow
      log.error(mongoUnitException.getMessage(), mongoUnitException);
      throw mongoUnitException;
    }
  }

  @Override
  public void afterEach(ExtensionContext context) {

    // Retrieve mongoDatabase and mongo unit properties from the extension namespace store
    Store extensionStore = getExtensionStore(context);
    MongoDatabase mongoDatabase = extensionStore.get(MONGODB_STORE_KEY, MongoDatabase.class);
    MongoUnitProperties mongoUnitProperties =
        extensionStore.get(MONGO_UNIT_PROPERTIES_KEY, MongoUnitProperties.class);

    // Retrieve class-level datasets from store
    MongoUnitDatasets classLevelMongoUnitDatasets =
        extensionStore.get(CLASS_MONGO_UNIT_DATASETS, MongoUnitDatasets.class);

    // Get method-level store, retrieve method dataset; remove method-level dataset from store
    Store methodStore = getMethodStore(context);
    MongoUnitDatasets methodLevelMongoUnitDatasets =
        methodStore.remove(METHOD_MONGO_UNIT_DATASETS, MongoUnitDatasets.class);

    // If no AssertMatchesDataset annotation are placed on either class or method, no assertion
    if (!classLevelMongoUnitDatasets.isAssertAnnotationPresent()
        && !methodLevelMongoUnitDatasets.isAssertAnnotationPresent()) {

      // No assert annotation, do nothing
      return;
    }

    // Combine class and method seed datasets
    List<MongoUnitCollection> expectedDataset =
        MongoUnitUtil.combineDatasets(
            classLevelMongoUnitDatasets.getAssertMatchesDatasets(),
            methodLevelMongoUnitDatasets.getAssertMatchesDatasets());

    // Retrieve actual dataset from database
    List<MongoUnitCollection> actualDataset = MongoUnitUtil.fromDatabase(mongoDatabase, null, null);

    // Perform assertion
    AssertionResult assertionResult;
    try {
      assertionResult = MongoUnitUtil
          .assertMatches(expectedDataset, actualDataset, mongoUnitProperties);
    } catch (Exception exception) {

      // Log error and rethrow
      log.error(exception.getMessage(), exception);
      throw exception;
    }

    // If did not match, throw assertion error exception
    if (!assertionResult.isMatch()) {
      throw new AssertionFailedError(assertionResult.getMessage());
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {

    // Remove mongo database and mongo properties reference from the class namespace store
    Store extensionStore = getExtensionStore(context);
    extensionStore.remove(MONGODB_STORE_KEY);
    extensionStore.remove(MONGO_UNIT_PROPERTIES_KEY);
    extensionStore.remove(CLASS_MONGO_UNIT_DATASETS);

    // Release reference to cached Mongo database
    CURRENT_MONGO_DATABASE = null;
  }

  /**
   * @param context Extension context in which execution occurs.
   * @return Store slice from the namespace of this extension.
   */
  private Store getExtensionStore(ExtensionContext context) {
    return context.getStore(NAMESPACE);
  }

  /**
   * @param context Extension context in which execution occurs.
   * @return Store slice from the namespace that includes this extension plus the testing method.
   */
  private Store getMethodStore(ExtensionContext context) {
    return context.getStore(Namespace.create(NAMESPACE, context.getRequiredTestMethod()));
  }
}

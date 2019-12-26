/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit;

import static org.mongounit.MongoUnitUtil.combineDatasets;
import static org.mongounit.MongoUnitUtil.extractMongoUnitDatasets;
import static org.mongounit.MongoUnitUtil.extractTestClassName;
import static org.mongounit.MongoUnitUtil.toMongoUnitCollections;
import static org.mongounit.MongoUnitUtil.toDatabase;
import static org.mongounit.config.MongoUnitConfigurationUtil.loadMongoUnitProperties;

import com.mongodb.client.MongoDatabase;
import java.util.List;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.mongounit.config.MongoUnitConfig;
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
   * Key with which to store {@link MongoUnitConfig} instance in the context store.
   */
  private static final String MONGO_UNIT_PROPERTIES_KEY = "mongoUnitProperties";

  /**
   * Key with which to store class-level {@link MongoUnitDatasets}.
   */
  private static final String CLASS_MONGO_UNIT_DATASETS_KEY = "classMongoUnitDatasets";

  /**
   * Key with which to store method-level {@link MongoUnitDatasets}.
   */
  private static final String METHOD_MONGO_UNIT_DATASETS_KEY = "methodMongoUnitDatasets";

  /**
   * Key with which to store test class name.
   */
  private static final String TEST_CLASS_NAME_KEY = "testClassName";

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
    MongoUnitConfig mongoUnitConfig = loadMongoUnitProperties();
    extensionStore.put(MONGO_UNIT_PROPERTIES_KEY, mongoUnitConfig);

    // Extract test class name based on the class and its MongoUnitTest annotation
    String testClassName = extractTestClassName(context.getRequiredTestClass());
    extensionStore.put(TEST_CLASS_NAME_KEY, testClassName);

    // Extract class-level datasets based on MongoUnit annotations
    MongoUnitDatasets mongoUnitDatasets = extractMongoUnitDatasets(context, testClassName, true);

    // Save class level datasets in the store
    extensionStore.put(CLASS_MONGO_UNIT_DATASETS_KEY, mongoUnitDatasets);
  }

  @Override
  public void beforeEach(ExtensionContext context) {

    // Retrieve mongoDatabase and mongo unit properties from the extension namespace store
    Store extensionStore = getExtensionStore(context);
    MongoDatabase mongoDatabase = extensionStore.get(MONGODB_STORE_KEY, MongoDatabase.class);
    MongoUnitConfig mongoUnitConfig =
        extensionStore.get(MONGO_UNIT_PROPERTIES_KEY, MongoUnitConfig.class);

    // Clear all collections out of the database
    MongoUnitUtil.dropAllCollectionsInDatabase(mongoDatabase);

    // Retrieve class-level datasets from store
    MongoUnitDatasets classLevelMongoUnitDatasets =
        extensionStore.get(CLASS_MONGO_UNIT_DATASETS_KEY, MongoUnitDatasets.class);

    // Retrieve test class name (derived either from MongoUnitTest annotation or simple class name)
    String testClassName = extensionStore.get(TEST_CLASS_NAME_KEY, String.class);

    // Extract method-level datasets based on MongoUnit annotations
    MongoUnitDatasets methodLevelMongoUnitDatasets =
        extractMongoUnitDatasets(context, testClassName, false);

    // Get method-level store and save method level dataset
    Store methodStore = getMethodStore(context);
    methodStore.put(METHOD_MONGO_UNIT_DATASETS_KEY, methodLevelMongoUnitDatasets);

    // Combine class and method seed datasets
    List<MongoUnitCollection> combinedDataset =
        combineDatasets(
            classLevelMongoUnitDatasets.getSeedWithDatasets(),
            methodLevelMongoUnitDatasets.getSeedWithDatasets());

    try {
      // Seed database with this dataset
      toDatabase(combinedDataset, mongoDatabase);

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
    MongoUnitConfig mongoUnitConfig =
        extensionStore.get(MONGO_UNIT_PROPERTIES_KEY, MongoUnitConfig.class);

    // Retrieve class-level datasets from store
    MongoUnitDatasets classLevelMongoUnitDatasets =
        extensionStore.get(CLASS_MONGO_UNIT_DATASETS_KEY, MongoUnitDatasets.class);

    // Get method-level store, retrieve method dataset; remove method-level dataset from store
    Store methodStore = getMethodStore(context);
    MongoUnitDatasets methodLevelMongoUnitDatasets =
        methodStore.remove(METHOD_MONGO_UNIT_DATASETS_KEY, MongoUnitDatasets.class);

    // If no AssertMatchesDataset annotation are placed on either class or method, no assertion
    if (!classLevelMongoUnitDatasets.isAssertAnnotationPresent()
        && !methodLevelMongoUnitDatasets.isAssertAnnotationPresent()) {

      // No assert annotation, do nothing
      return;
    }

    // Combine class and method seed datasets
    List<MongoUnitCollection> expectedDataset =
        combineDatasets(
            classLevelMongoUnitDatasets.getAssertMatchesDatasets(),
            methodLevelMongoUnitDatasets.getAssertMatchesDatasets());

    // Retrieve actual dataset from database
    List<MongoUnitCollection> actualDataset = toMongoUnitCollections(mongoDatabase, null, null);

    // Perform assertion
    AssertionResult assertionResult;
    try {
      assertionResult = MongoUnitUtil
          .assertMatches(expectedDataset, actualDataset);
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
    extensionStore.remove(CLASS_MONGO_UNIT_DATASETS_KEY);
    extensionStore.remove(TEST_CLASS_NAME_KEY);

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

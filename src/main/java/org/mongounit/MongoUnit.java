package org.mongounit;

import static org.mongounit.MongoUnitExtension.CURRENT_MONGO_DATABASE;
import static org.mongounit.config.MongoUnitConfigurationUtil.loadMongoUnitProperties;

import com.mongodb.client.MongoDatabase;
import java.util.List;
import org.mongounit.config.MongoUnitProperties;
import org.mongounit.model.AssertionResult;
import org.mongounit.model.MongoUnitCollection;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MongoUnit} class contains utilities for manually seeding and performing assertion
 * matches.
 */
public class MongoUnit {

  /**
   * Logger for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(MongoUnit.class);

  /**
   * Finds and drops all of the collections in the database that the MongoUnit framework is
   * configured to automatically connect to.
   */
  public static void dropAllCollectionsInDatabase() {

    MongoDatabase mongoDatabase = CURRENT_MONGO_DATABASE;
    MongoUnitUtil.dropAllCollectionsInDatabase(mongoDatabase);
  }

  /**
   * Seeds the database with the JSON-based datasets pointed to by the provided 'locations' path
   * array. Returns a list of {@link MongoUnitCollection}s that can optionally be reused for
   * assertions in the 'assertMatches*' methods.
   *
   * @param locations Array paths to the files containing datasets.
   * @param locationType Type of location the provided 'locations' are.
   * @param relativePackageClass If 'locationType' is 'PACKAGE', this is the class type whose
   * package should be used for package relative 'location' path. Otherwise, it's ignored and can be
   * null.
   * @return List of {@link MongoUnitCollection}s based on the data pointed to by provided
   * 'locations'.
   * @throws MongoUnitException If something goes wrong with loading datasets from the specified
   * locations.
   */
  @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
  public static List<MongoUnitCollection> seedWithDataset(
      String[] locations,
      LocationType locationType,
      Class<?> relativePackageClass) throws MongoUnitException {

    // Load dataset from provided locations
    List<MongoUnitCollection> seedWithDataset =
        loadDatasetFromLocations(locations, locationType, relativePackageClass);

    // Seed database
    MongoUnitProperties mongoUnitProperties = loadMongoUnitProperties();
    MongoDatabase mongoDatabase = CURRENT_MONGO_DATABASE;
    MongoUnitUtil.toDatabase(seedWithDataset, mongoDatabase, mongoUnitProperties);

    return seedWithDataset;
  }

  /**
   * Seeds the database with the JSON-based datasets pointed to by the provided 'locations' path
   * array. Returns a list of {@link MongoUnitCollection}s that can optionally be reused for
   * assertions in the 'assertMatches*' methods.
   *
   * @param locations Array paths to the files containing datasets. The locations are assumed to be
   * relative to the classpath root.
   * @return List of {@link MongoUnitCollection}s based on the data pointed to by provided
   * 'locations'.
   */
  @SuppressWarnings("UnusedReturnValue")
  public static List<MongoUnitCollection> seedWithDataset(String[] locations) {
    return seedWithDataset(locations, LocationType.CLASSPATH_ROOT, null);
  }

  /**
   * Seeds the database with the JSON-based datasets pointed to by the provided 'locations' path
   * array. Returns a list of {@link MongoUnitCollection}s that can optionally be reused for
   * assertions in the 'assertMatches*' methods.
   *
   * @param location Path to the file containing a dataset. The location is assumed to be relative
   * to the classpath root.
   * @return List of {@link MongoUnitCollection}s based on the data pointed to by provided
   * 'location'.
   */
  @SuppressWarnings("UnusedReturnValue")
  public static List<MongoUnitCollection> seedWithDataset(String location) {
    return seedWithDataset(new String[]{location}, LocationType.CLASSPATH_ROOT, null);
  }

  /**
   * Asserts that whatever is currently in the database connected to by the MongoUnit framework
   * matches the JSON datasets contained in files pointed to by the provided 'locations' array of
   * paths.
   *
   * @param locations Array paths to the files containing datasets.
   * @param locationType Type of location the provided 'locations' are.
   * @param relativePackageClass If 'locationType' is 'PACKAGE', this is the class type whose
   * package should be used for package relative 'location' path. Otherwise, it's ignored and can be
   * null.
   * @throws MongoUnitException If something goes wrong with loading datasets from the specified
   * locations.
   */
  @SuppressWarnings("WeakerAccess")
  public static void assertMatchesDataset(
      String[] locations,
      LocationType locationType,
      Class<?> relativePackageClass) throws MongoUnitException {

    // Load dataset from provided locations
    List<MongoUnitCollection> expectedDataset =
        loadDatasetFromLocations(locations, locationType, relativePackageClass);

    MongoDatabase mongoDatabase = CURRENT_MONGO_DATABASE;

    // Retrieve actual dataset from database
    List<MongoUnitCollection> actualDataset = MongoUnitUtil.fromDatabase(mongoDatabase, null, null);

    // Perform assertion
    performAssertion(expectedDataset, actualDataset);
  }

  /**
   * Asserts that whatever is currently in the database connected to by the MongoUnit framework
   * matches the JSON datasets contained in files pointed to by the provided classpath root relative
   * 'locations' array of paths.
   *
   * @param locations Array paths to the files containing datasets. The locations are assumed to be
   * classpath root relative.
   * @throws MongoUnitException If something goes wrong with loading datasets from the specified
   * locations.
   */
  public static void assertMatchesDataset(String[] locations) throws MongoUnitException {
    assertMatchesDataset(locations, LocationType.CLASSPATH_ROOT, null);
  }

  /**
   * Asserts that whatever is currently in the database connected to by the MongoUnit framework
   * matches the JSON dataset contained in the file pointed to by the provided classpath root
   * relative 'location'.
   *
   * @param location Path to the file containing a dataset. The location is assumed to be classpath
   * root relative.
   * @throws MongoUnitException If something goes wrong with loading the dataset from the specified
   * location.
   */
  public static void assertMatchesDataset(String location) throws MongoUnitException {
    assertMatchesDataset(new String[]{location}, LocationType.CLASSPATH_ROOT, null);
  }

  /**
   * Asserts that whatever is currently in the database connected to by the MongoUnit framework
   * matches the dataset collectively contained in the provided 'datasets'.
   *
   * @param expectedDatasets List of {@link MongoUnitCollection}s that collectively represent the
   * dataset with which to compare the actual database state with. Note that the {@link
   * MongoUnitCollection}s can repeat the same-named collection more than once. This method will
   * combine the documents in the same-named collections into 1 same-named collection while keeping
   * the original order of the documents.
   * @throws MongoUnitException If something goes wrong interpreting the comparisons contained in
   * the provided 'datasets'.
   */
  @SuppressWarnings("WeakerAccess")
  public static void assertMatchesDataset(List<MongoUnitCollection> expectedDatasets)
      throws MongoUnitException {

    MongoDatabase mongoDatabase = CURRENT_MONGO_DATABASE;

    // Retrieve actual dataset from database
    List<MongoUnitCollection> actualDataset = MongoUnitUtil.fromDatabase(mongoDatabase, null, null);

    // Combine so there are no same-named repeated collections are present
    List<MongoUnitCollection> expectedDataset = MongoUnitUtil
        .combineNoRepeatingCollections(expectedDatasets);

    // Perform assertion
    performAssertion(expectedDataset, actualDataset);
  }

  /**
   * Assert database is empty.
   *
   * @throws MongoUnitException If something goes wrong during the assertion.
   */
  public static void assertMatchesEmptyDataset() throws MongoUnitException {
    assertMatchesDataset((List<MongoUnitCollection>) null);
  }

  /**
   * Returns a list of {@link MongoUnitCollection}s that represent the JSON-based data pointed to by
   * the provided 'locations' array of paths.
   *
   * @param locations Array paths to the files containing datasets.
   * @param locationType Type of location the provided 'locations' are.
   * @param relativePackageClass If 'locationType' is 'PACKAGE', this is the class type whose
   * package should be used for package relative 'location' path. Otherwise, it's ignored and can be
   * null.
   * @return List of {@link MongoUnitCollection}s that represent the JSON-based data pointed to by
   * the provided 'locations' array of paths.
   * @throws MongoUnitException If something goes wrong with loading datasets from the specified
   * 'locations'.
   */
  @SuppressWarnings("WeakerAccess")
  public static List<MongoUnitCollection> loadDatasetFromLocations(
      String[] locations,
      LocationType locationType,
      Class<?> relativePackageClass) throws MongoUnitException {

    // Check that locations is not null or empty
    checkLocations(locations);

    // Retrieve dataset content and convert/collect to MongoUnitCollection
    List<MongoUnitCollection> tempSeedWithDataset =
        MongoUnitUtil.retrieveDatasetFromLocations(locations, locationType, relativePackageClass);

    // Combine so there are no same-named repeated collections are present
    return MongoUnitUtil.combineNoRepeatingCollections(tempSeedWithDataset);
  }

  /**
   * Returns a list of {@link MongoUnitCollection}s that represent the JSON-based data pointed to by
   * the provided 'locations' array of paths. The 'locations' are assumed to be classpath root
   * relative.
   *
   * @param locations Array paths to the files containing datasets. The 'locations' are assumed to
   * be classpath root relative.
   * @return List of {@link MongoUnitCollection}s that represent the JSON-based data pointed to by
   * the provided 'locations' array of paths.
   * @throws MongoUnitException If something goes wrong with loading datasets from the specified
   * 'locations'.
   */
  public static List<MongoUnitCollection> loadDatasetFromLocations(String[] locations)
      throws MongoUnitException {
    return loadDatasetFromLocations(locations, LocationType.CLASSPATH_ROOT, null);
  }

  /**
   * Returns a list of {@link MongoUnitCollection}s that represent the JSON-based data pointed to by
   * the provided 'locations' array of paths. The 'locations' are assumed to be classpath root
   * relative.
   *
   * @param location Path to the file containing a dataset. The 'location' are assumed to be
   * classpath root relative.
   * @return List of {@link MongoUnitCollection}s that represent the JSON-based data pointed to by
   * the provided 'location' path.
   * @throws MongoUnitException If something goes wrong with loading dataset from the specified
   * 'location'.
   */
  public static List<MongoUnitCollection> loadDatasetFromLocation(String location)
      throws MongoUnitException {
    return loadDatasetFromLocations(new String[]{location}, LocationType.CLASSPATH_ROOT, null);
  }

  /**
   * Checks that locations is not null or empty. If it is, throws an exception.
   *
   * @param locations Array of location paths.
   * @throws MongoUnitException If the provided 'locations' is null or an empty array.
   */
  private static void checkLocations(String[] locations) throws MongoUnitException {

    // Check that locations is not null or empty
    if (locations == null || locations.length == 0) {

      String message = "Error: locations must not be null or empty";
      log.error(message);
      throw new MongoUnitException(message);
    }
  }

  /**
   * Performs assertion comparing the provided 'expectedDataset' with the provided 'actualDataset'.
   *
   * @param expectedDataset Expected dataset to compare.
   * @param actualDataset Actual dataset to compare.
   * @throws MongoUnitException If something goes wrong interpreting the comparisons contained in
   * the provided 'datasets'.
   */
  private static void performAssertion(
      List<MongoUnitCollection> expectedDataset,
      List<MongoUnitCollection> actualDataset) throws MongoUnitException {

    MongoUnitProperties mongoUnitProperties = loadMongoUnitProperties();

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
}

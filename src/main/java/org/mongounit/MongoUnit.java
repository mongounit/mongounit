/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit;

import static org.mongounit.MongoUnitExtension.CURRENT_MONGO_DATABASE;
import static org.mongounit.MongoUnitUtil.extractTestClassName;
import static org.mongounit.MongoUnitUtil.toMongoUnitCollections;
import static org.mongounit.MongoUnitUtil.retrieveDatasetFromLocations;
import static org.mongounit.MongoUnitUtil.toDatabase;
import static org.mongounit.config.MongoUnitConfigurationUtil.loadMongoUnitProperties;

import com.mongodb.client.MongoDatabase;
import java.util.List;
import org.mongounit.config.MongoUnitConfig;
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
   * @param relativePackageClass If 'locationType' is 'CLASS', this is the class type whose packaged
   * location should be used for relativity of the 'location' path. Otherwise, it's ignored and can
   * be null.
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
    MongoUnitConfig mongoUnitConfig = loadMongoUnitProperties();
    MongoDatabase mongoDatabase = CURRENT_MONGO_DATABASE;
    toDatabase(seedWithDataset, mongoDatabase, mongoUnitConfig);

    return seedWithDataset;
  }

  /**
   * Seeds the database with the JSON-based datasets pointed to by the provided 'locations' path
   * array. Returns a list of {@link MongoUnitCollection}s that can optionally be reused for
   * assertions in the 'assertMatches*' methods.
   *
   * @param locations Array of paths to the files containing datasets. The locations type is
   * 'CLASS'. See javadoc org.mongounit.LocationType#CLASS for more detail.
   * @param testClass Class instance of the test class.
   * @return List of {@link MongoUnitCollection}s based on the data pointed to by provided
   * 'locations' and 'testClass'.
   */
  @SuppressWarnings("UnusedReturnValue")
  public static List<MongoUnitCollection> seedWithDataset(String[] locations, Class<?> testClass) {
    return seedWithDataset(locations, LocationType.CLASS, testClass);
  }

  /**
   * Seeds the database with the JSON-based datasets pointed to by the provided 'locations' path
   * array. Returns a list of {@link MongoUnitCollection}s that can optionally be reused for
   * assertions in the 'assertMatches*' methods.
   *
   * @param location Path to the file containing a dataset. The location type is 'CLASS'. See
   * javadoc org.mongounit.LocationType#CLASS for more detail.
   * @param testClass Class instance of the test class.
   * @return List of {@link MongoUnitCollection}s based on the data pointed to by provided
   * 'location' and 'testClass'.
   */
  @SuppressWarnings("UnusedReturnValue")
  public static List<MongoUnitCollection> seedWithDataset(String location, Class<?> testClass) {
    return seedWithDataset(new String[]{location}, LocationType.CLASS, testClass);
  }

  /**
   * Asserts that whatever is currently in the database connected to by the MongoUnit framework
   * matches the JSON datasets contained in files pointed to by the provided 'locations' array of
   * paths.
   *
   * @param locations Array paths to the files containing datasets.
   * @param locationType Type of location the provided 'locations' are.
   * @param relativePackageClass If 'locationType' is 'CLASS', this is the class type whose package
   * should be used for package relative 'location' path. Otherwise, it's ignored and can be null.
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
    List<MongoUnitCollection> actualDataset = toMongoUnitCollections(mongoDatabase, null, null);

    // Perform assertion
    performAssertion(expectedDataset, actualDataset);
  }

  /**
   * Asserts that whatever is currently in the database connected to by the MongoUnit framework
   * matches the JSON datasets contained in files pointed to by the provided 'locations' array of
   * paths. The locations type is 'CLASS'. See javadoc org.mongounit.LocationType#CLASS for more
   * detail.
   *
   * @param locations Array paths to the files containing datasets. The locations type is 'CLASS' .
   * See javadoc org.mongounit.LocationType#CLASS for more detail.
   * @param testClass Class instance of the test class.
   * @throws MongoUnitException If something goes wrong with loading datasets from the specified
   * locations.
   */
  public static void assertMatchesDataset(
      String[] locations,
      Class<?> testClass) throws MongoUnitException {
    assertMatchesDataset(locations, LocationType.CLASS, testClass);
  }

  /**
   * Asserts that whatever is currently in the database connected to by the MongoUnit framework
   * matches the JSON datasets contained in files pointed to by the provided 'location' path. The
   * location type is 'CLASS'. See javadoc org.mongounit.LocationType#CLASS for more detail.
   *
   * @param location Path to the file containing a dataset. The location type is 'CLASS'. See
   * javadoc org.mongounit.LocationType#CLASS for more detail.
   * @param testClass Class instance of the test class.
   * @throws MongoUnitException If something goes wrong with loading the dataset from the specified
   * location.
   */
  public static void assertMatchesDataset(
      String location,
      Class<?> testClass) throws MongoUnitException {
    assertMatchesDataset(new String[]{location}, LocationType.CLASS, testClass);
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
    List<MongoUnitCollection> actualDataset = toMongoUnitCollections(mongoDatabase, null, null);

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
    assertMatchesDataset(null);
  }

  /**
   * Returns a list of {@link MongoUnitCollection}s that represent the JSON-based data pointed to by
   * the provided 'locations' array of paths.
   *
   * @param locations Array paths to the files containing datasets.
   * @param locationType Type of location the provided 'locations' are.
   * @param relativePackageClass If 'locationType' is 'CLASS', this is the class type whose package
   * should be used for package relative 'location' path. Otherwise, it's ignored and can be
   * 'null'.
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
    String testClassName = extractTestClassName(relativePackageClass);
    List<MongoUnitCollection> tempSeedWithDataset =
        retrieveDatasetFromLocations(locations, locationType, relativePackageClass, testClassName);

    // Combine so there are no same-named repeated collections are present
    return MongoUnitUtil.combineNoRepeatingCollections(tempSeedWithDataset);
  }

  /**
   * Returns a list of {@link MongoUnitCollection}s that represent the JSON-based data pointed to by
   * the provided 'locations' array of paths. The locations type is 'CLASS'. See javadoc org
   * .mongounit.LocationType#CLASS for more detail.
   *
   * @param locations Array paths to the files containing datasets. The locations type is 'CLASS' .
   * See javadoc org.mongounit.LocationType#CLASS for more detail.
   * @param testClass Class instance of the test class.
   * @return List of {@link MongoUnitCollection}s that represent the JSON-based data pointed to by
   * the provided 'locations' array of paths.
   * @throws MongoUnitException If something goes wrong with loading datasets from the specified
   * 'locations'.
   */
  public static List<MongoUnitCollection> loadDatasetFromLocations(
      String[] locations,
      Class<?> testClass)
      throws MongoUnitException {
    return loadDatasetFromLocations(locations, LocationType.CLASS, testClass);
  }

  /**
   * Returns a list of {@link MongoUnitCollection}s that represent the JSON-based data pointed to by
   * the provided 'locations' array of paths. The location type is 'CLASS'. See javadoc
   * org.mongounit.LocationType#CLASS for more detail.
   *
   * @param location Path to the file containing a dataset. The location type is 'CLASS'. See
   * javadoc org.mongounit.LocationType#CLASS for more detail.
   * @param testClass Class instance of the test class.
   * @return List of {@link MongoUnitCollection}s that represent the JSON-based data pointed to by
   * the provided 'location' path.
   * @throws MongoUnitException If something goes wrong with loading dataset from the specified
   * 'location'.
   */
  public static List<MongoUnitCollection> loadDatasetFromLocation(
      String location,
      Class<?> testClass)
      throws MongoUnitException {

    return loadDatasetFromLocations(
        new String[]{location},
        LocationType.CLASS,
        testClass);
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

    MongoUnitConfig mongoUnitConfig = loadMongoUnitProperties();

    // Perform assertion
    AssertionResult assertionResult;
    try {
      assertionResult = MongoUnitUtil
          .assertMatches(expectedDataset, actualDataset, mongoUnitConfig);
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

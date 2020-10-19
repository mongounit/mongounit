/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit;

import static org.mongounit.MongoUnitUtil.fromDatabase;
import static org.mongounit.config.MongoUnitProperties.DEFAULT_MONGO_UNIT_VALUE_INDICATOR_FIELD_NAME;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.mongounit.config.MongoUnitProperties;
import org.mongounit.model.MongoUnitCollection;

/**
 * {@link DatasetGenerator} class is a standalone runnable program that can generate JSON-based
 * starter dataset files which can later be used to seed and verify database tests.
 */
public class DatasetGenerator {

  /**
   * Argument name for the database URI.
   */
  private static final String DB_URI_ARG_NAME = "dbUri";

  /**
   * Argument name for the comma-separated list of collection names.
   */
  private static final String COLLECTION_NAMES_ARG_NAME = "collectionNames";

  /**
   * Argument name for the output file path.
   */
  private static final String OUTPUT_PATH_ARG_NAME = "output";

  /**
   * Argument name for the list of BSON types that need to be preserved in seed output.
   */
  private static final String PRESERVE_BSON_TYPES_ARG_NAME = "preserveBsonTypes";

  /**
   * Argument name for the field name to use as an indicator in developer JSON files to signify that
   * a document is a representation of a special MongoUnit value. See {@link
   * org.mongounit.config.MongoUnitProperties} for more details.
   */
  private static final String MONGO_UNIT_VALUE_FIELD_NAME_INDICATOR_ARG_NAME =
      "mongoUnitValueFieldNameIndicator";

  /**
   * Default name of the output file.
   */
  private static final String DEFAULT_OUTPUT_FILE_NAME = "output.json";

  /**
   * Default output file path if '-output' is omitted.
   */
  private static final String DEFAULT_OUTPUT_PATH =
      FileSystems.getDefault().getPath("./" + DEFAULT_OUTPUT_FILE_NAME).toAbsolutePath().toString();

  /**
   * Default list of BSON types that need to be preserved in seed output.
   */
  private static final List<String> DEFAULT_PRESERVE_BSON_TYPES =
      Arrays.asList("OBJECT_ID", "DATE_TIME");

  /**
   * Main method for the {@link DatasetGenerator}.
   *
   * @param args Arguments that are passed into the standalone program.
   */
  public static void main(String[] args) {

    // Extract command line argument values
    DatasetGeneratorArguments arguments = extractArgumentValues(args);

    // Connect to mongo DB
    MongoDatabase mongoDatabase = getMongoDatabase(arguments);

    // Setup and modify indicator based on argument MongoUnit properties
    MongoUnitProperties mongoUnitProperties =
        new MongoUnitProperties(
            null,
            null,
            arguments.getMongoUnitValueFieldNameIndicator(),
            null,
            null);

    // Extract MongoUnitCollections
    List<MongoUnitCollection> mongoUnitCollections = null;
    try {

      mongoUnitCollections = fromDatabase(
          mongoDatabase,
          mongoUnitProperties,
          arguments.getPreserveBsonTypes(),
          arguments.getCollectionNames().toArray(new String[0]));
    } catch (IllegalArgumentException exception) {

      System.out.println("**** ERROR: " + exception.getMessage());
      printRules();
      System.exit(-1);
    }

    // Output JSON file with data
    outputAsJson(arguments, mongoUnitCollections);
  }

  /**
   * @param argumentValues Values of arguments provided by the user on the command line.
   * @return Instance of {@link MongoDatabase} that's already connected to a particular MongoDB
   * database.
   */
  private static MongoDatabase getMongoDatabase(DatasetGeneratorArguments argumentValues) {

    MongoClient mongoClient = new MongoClient(argumentValues.getMongoClientURI());
    String dbName = argumentValues.getMongoClientURI().getDatabase();

    // Make sure database name is specified
    if (dbName == null) {
      System.out.println("**** ERROR: Database name must be specified as part of the '-dbUri'.");
      printRules();
      System.exit(-1);
    }

    return mongoClient.getDatabase(dbName);
  }

  /**
   * Writes JSON representation of the provided 'mongoUnitCollections' to file (erasing previous
   * version of the file) at location provided by 'arguments.getOutputPath'.
   *
   * @param argumentValues Values of arguments provided by the user on the command line.
   * @param mongoUnitCollections List of {@link MongoUnitCollection}s to convert to JSON.
   */
  private static void outputAsJson(
      DatasetGeneratorArguments argumentValues,
      List<MongoUnitCollection> mongoUnitCollections) {

    // Convert mongo unit collection data into JSON
    String jsonMongoUnitCollection = null;
    ObjectMapper jsonMapper = new ObjectMapper();
    try {
      jsonMongoUnitCollection =
          jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(mongoUnitCollections);
    } catch (JsonProcessingException exception) {
      System.out.println("**** ERROR: " + exception.getMessage());
      System.exit(-1);
    }

    // Output JSON file to where 'output' argument specified
    File outputFile = new File(argumentValues.getOutputPath());
    try (BufferedWriter writer =
        new BufferedWriter(new FileWriter(argumentValues.getOutputPath()))) {

      writer.write(jsonMongoUnitCollection);

      // Output completion message
      System.out.println();
      System.out.println("**************************");
      System.out.println("**** JSON was written to " + outputFile.getCanonicalPath());
      System.out.println("**************************");

    } catch (IOException exception) {
      System.out
          .println("**** ERROR: Could not write to output file: " + argumentValues.getOutputPath()
              + ". Exception: " + exception.getMessage());
      System.exit(-1);
    }
  }

  /**
   * @param args Arguments passed on the command line.
   * @return Fully resolved {@link DatasetGeneratorArguments}.
   */
  private static DatasetGeneratorArguments extractArgumentValues(String[] args) {

    // Init arg values
    MongoClientURI mongoClientURI = null;
    List<String> collectionNames = new ArrayList<>();
    String outputPath = null;
    List<String> preserveBsonTypes = DEFAULT_PRESERVE_BSON_TYPES;
    String mongoUnitValueFieldNameIndicator = DEFAULT_MONGO_UNIT_VALUE_INDICATOR_FIELD_NAME;

    // Loop over argument and extract values
    for (String argument : args) {

      // Make sure "=" is present in the argument
      int equalsIndex = argument.indexOf("=");
      if (equalsIndex == -1) {
        System.out.println("**** ERROR: argument '" + argument + "' incorrectly formatted.");
        printRules();
        System.exit(-1);
      }

      // Extract argument name (remove '-' and consider name up until '=')
      String argName = argument.substring(1, equalsIndex);
      String argValue = argument.substring(equalsIndex + 1);

      // Extract value based on argument type
      switch (argName) {

        case DB_URI_ARG_NAME:

          // Create MongoDB URI and test for validity of the URI in the process
          try {
            mongoClientURI = new MongoClientURI(argValue);
          } catch (Exception exception) {

            // Show error to user and exit with error code
            System.out.println("**** ERROR: " + exception.getMessage());
            printRules();
            System.exit(-1);
          }

          break;

        case COLLECTION_NAMES_ARG_NAME:

          // Extract collection names; don't add any that are empty strings
          collectionNames = extractListArgumentValues(argValue);

          // Check if any collection names left; if not, show error to user and exist with error
          if (collectionNames.isEmpty()) {

            // Show error to user and exit with error code
            System.out.println("**** ERROR: if specifying '-collectionNames', must specify at"
                + " least 1 collection name.");
            printRules();
            System.exit(-1);
          }

          break;

        case OUTPUT_PATH_ARG_NAME:

          // Convert path to be an absolute path
          outputPath =
              FileSystems.getDefault().getPath(argValue).toAbsolutePath().toString();

          // If file name doesn't contain '.json', append 'output.json' to path
          File outputFile = new File(outputPath);
          String fileName = outputFile.getName();
          if (!fileName.endsWith(".json") && !fileName.endsWith(".JSON")) {

            // If doens't end with .json, check that what was provided is an existing directory then
            if (!outputFile.isDirectory()) {
              System.out.println("**** ERROR: directory in the path must already exist. '-output'"
                  + " value that does not end with '.json' is assumed to be a directory.");
              printRules();
              System.exit(-1);
            }

            // If file name with '.json' isn't specified, assume directory and append file name
            outputPath += "/" + DEFAULT_OUTPUT_FILE_NAME;
          }

          break;

        case PRESERVE_BSON_TYPES_ARG_NAME:

          // Extract BSON types to preserve
          preserveBsonTypes = extractListArgumentValues(argValue);

          break;

        case MONGO_UNIT_VALUE_FIELD_NAME_INDICATOR_ARG_NAME:

          mongoUnitValueFieldNameIndicator = argValue;

          break;

        default:

          // Print error, rules and exist with an error code
          System.out.println("**** ERROR: unrecognized argument: '" + argName + "'.");
          printRules();
          System.exit(-1);
      }
    }

    // If outputPath was not specified, use default
    if (outputPath == null) {
      outputPath = DEFAULT_OUTPUT_PATH;
    }

    // If dbUri is not specified
    if (mongoClientURI == null) {

      System.out.println("**** ERROR: -dbUri must be specified.");
      printRules();
      System.exit(-1);
    }

    return DatasetGeneratorArguments.builder()
        .mongoClientURI(mongoClientURI)
        .collectionNames(collectionNames)
        .outputPath(outputPath)
        .preserveBsonTypes(preserveBsonTypes)
        .mongoUnitValueFieldNameIndicator(mongoUnitValueFieldNameIndicator)
        .build();
  }

  /**
   * Prints rules of how to call this program to the terminal.
   */
  private static void printRules() {

    System.out.println("**************************");
    System.out.println("* Usage: ");
    System.out.println("* java -jar mongounit-x.x.x-jar-with-dependencies.jar .jar"
        + " -dbUri=mongodb://localhost:27017/test_db"
        + " -collectionNames=col1,col2"
        + " -output=./output.json");
    System.out.println("*");
    System.out.println("* Individual arguments must not have any spaces between '=' and"
        + " argument value or even in the argument value itself.");
    System.out.println("* '-dbUri' (required) must be a valid MongoDB URI. Must start with"
        + " 'mongodb'. Can contain username/password.");
    System.out.println("* '-output' (optional) is an absolute or relative path to the file that"
        + " should be created with the dataset output in JSON format. An existing file with the"
        + " same name will be erased. If '-output' is specified, it MUST end with '.json'."
        + " Defaults to './output.json' if '-output' is omitted.");
    System.out.println("* '-collectionNames' (optional) comma separated list of collection to"
        + " limit dataset generation to. No spaces allowed between collection names. Defaults to"
        + " all collections in the database.");
    System.out.println("* '-preserveBsonTypes' (optional) comma separated list of BSON types"
        + " to generate explicit MongoUnit BSON type specification for. The string types are enum"
        + " names from the org.bson.BsonType. If not specified, defaults to OBJECT_ID and"
        + " DATE_TIME.");
    System.out.println("* '-mongoUnitValueFieldNameIndicator' (optional) field name to use in"
        + " developer JSON files to signify that a document is a representation of a special"
        + " MongoUnit value. If not specified, defaults to $$.");
    System.out.println("**************************");
    System.out.println();
  }

  /**
   * @param commaSeparateArgumentValue String that contains comma separated argument values without
   * a space between them.
   * @return List of argument values, where each item in the list is an argument value from the
   * 'commaSeparateArgumentValue'
   */
  public static List<String> extractListArgumentValues(String commaSeparateArgumentValue) {

    List<String> argumentValueList = new ArrayList<>();

    // Extract collection names; don't add any that are empty strings
    String[] argumentValueArray = commaSeparateArgumentValue.split(",");
    for (String argumentValue : argumentValueArray) {

      if (!argumentValue.trim().isEmpty()) {
        argumentValueList.add(argumentValue.trim());
      }
    }

    return argumentValueList;
  }
}

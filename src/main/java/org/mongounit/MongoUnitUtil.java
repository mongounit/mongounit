/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonBoolean;
import org.bson.BsonDateTime;
import org.bson.BsonDbPointer;
import org.bson.BsonDecimal128;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonJavaScript;
import org.bson.BsonNull;
import org.bson.BsonObjectId;
import org.bson.BsonRegularExpression;
import org.bson.BsonString;
import org.bson.BsonSymbol;
import org.bson.BsonTimestamp;
import org.bson.BsonUndefined;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mongounit.config.MongoUnitProperties;
import org.mongounit.model.AssertionResult;
import org.mongounit.model.MongoUnitAnnotations;
import org.mongounit.model.MongoUnitCollection;
import org.mongounit.model.MongoUnitDatasets;
import org.mongounit.model.MongoUnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoUnitUtil {

  /**
   * Logger for this configuration class.
   */
  private static Logger log = LoggerFactory.getLogger(MongoUnitUtil.class);

  /**
   * Field name to use when extracting the comparator field out of a special document which
   * represents a MongoUnit value.
   */
  public static final String COMPARATOR_FIELD_NAME = "comparator";

  /**
   * Format in which a date is expected to appear in the seed or expected JSON documents.
   */
  private static final String DATE_STRING_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  /**
   * Returns a list of {@link MongoUnitCollection}s that represents the dataset stored in the
   * provided 'mongoDatabase'.
   *
   * @param mongoDatabase Instance of the MongoDB database with collections based on which to base
   * the returned dataset.
   * @param mongoUnitProperties Collection of properties framework was configured with. If the
   * provided 'preserveBsonTypes' is null, this argument may also be 'null' and is ignored.
   * @param preserveBsonTypes List of string representation of {@link org.bson.BsonType} enum names
   * that should be preserved when creating documents. This **must** be 'null' when this method is
   * used for assertions instead of to output JSON through {@link DatasetGenerator}.
   * @param collectionNames Optional list of collection names to which to restrict data extraction
   * to.
   * @return List of {@link MongoUnitCollection}s that represents the dataset stored in the provided
   * 'mongoDatabase'. If 'collectionNames' are specified, the extracted dataset will be limited to
   * those collections only.
   * @throws IllegalArgumentException If at least one of the optionally specified 'collectionNames'
   * does not exist in the provided 'mongoDatabase'.
   */
  public static List<MongoUnitCollection> fromDatabase(
      MongoDatabase mongoDatabase,
      MongoUnitProperties mongoUnitProperties,
      List<String> preserveBsonTypes,
      String... collectionNames) throws IllegalArgumentException {

    List<MongoUnitCollection> mongoUnitCollections = new ArrayList<>();
    List<String> collectionNamesToExtract = getCollectionNamesToUse(mongoDatabase, collectionNames);

    // Extract documents from each collection
    for (String collectionName : collectionNamesToExtract) {

      MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

      // Extract mongo unit documents (comprised of name/value maps) from single DB collection
      List<Map<String, Object>> mongoUnitDocuments =
          getMongoUnitDocuments(collection, mongoUnitProperties, preserveBsonTypes);

      // Create MongoUnitCollection and add it to the list
      MongoUnitCollection mongoUnitCollection = MongoUnitCollection.builder()
          .collectionName(collectionName)
          .documents(mongoUnitDocuments)
          .build();
      mongoUnitCollections.add(mongoUnitCollection);
    }

    return mongoUnitCollections;
  }

  /**
   * Seeds an existing database provided by 'mongoDatabase' with dataset represented in the {@link
   * MongoUnitCollection}s schema by the provided 'jsonMongoUnitCollections'.
   *
   * @param mongoUnitCollections List of {@link MongoUnitCollection}s to persist to seed the
   * database with.
   * @param mongoDatabase MongoDB instance to seed with the provided data.
   * @param mongoUnitProperties Collection of properties framework was configured with.
   * @throws MongoUnitException If anything goes wrong with interpreting the provided
   * 'mongoUnitCollections' in order to seed the database.
   */
  public static void toDatabase(
      List<MongoUnitCollection> mongoUnitCollections,
      MongoDatabase mongoDatabase,
      MongoUnitProperties mongoUnitProperties) throws MongoUnitException {

    // Bulk insert bson documents for each collection
    for (MongoUnitCollection mongoUnitCollection : mongoUnitCollections) {

      String collectionName = mongoUnitCollection.getCollectionName();

      // Convert mongo unit collection to BSON documents
      List<Document> collectionDocs;
      try {

        collectionDocs = toBsonDocuments(mongoUnitCollection.getDocuments(), mongoUnitProperties);

      } catch (MongoUnitException mongoUnitException) {

        // Add tracing to the exception message
        String message = "Collection '" + collectionName + "': ";
        throw new MongoUnitException(message + mongoUnitException.getMessage(), mongoUnitException);
      }

      // Bulk insert collection docs into the collection
      MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);
      mongoCollection.insertMany(collectionDocs);
    }
  }

  /**
   * Finds and drops all of the collections in the provided 'mongoDatabase'.
   *
   * @param mongoDatabase MongoDB reference in which to clear/drop all collections.
   */
  public static void dropAllCollectionsInDatabase(MongoDatabase mongoDatabase) {

    // Iterate over all collections in the db and drop them
    for (String collectionName : mongoDatabase.listCollectionNames()) {

      mongoDatabase.getCollection(collectionName).drop();

      log.trace("Dropped collection " + collectionName);
    }
  }

  /**
   * @param jsonMongoUnitCollections String JSON representation that conforms to the {@link
   * MongoUnitCollection}s schema.
   * @return List of {@link MongoUnitCollection} objects represented by the provided JSON string
   * 'jsonMongoUnitCollections'.
   * @throws MongoUnitException If the provided 'jsonMongoUnitCollections' can not be interpreted to
   * match the list of {@link MongoUnitCollection}s.
   */
  @SuppressWarnings("WeakerAccess")
  public static List<MongoUnitCollection> toMongoUnitTypedCollectionsFromJson(
      String jsonMongoUnitCollections) throws MongoUnitException {

    try {

      ObjectMapper jsonMapper = new ObjectMapper();

      return jsonMapper.readValue(
          jsonMongoUnitCollections,
          new TypeReference<>() {
          });
    } catch (IOException exception) {

      String message = "Unable to interpret JSON dataset. " + exception.getMessage();
      log.error(message);
      throw new MongoUnitException(message, exception);
    }
  }

  /**
   * @param mongoDatabase Database which collection names will be extracted from.
   * @param collectionNames Possibly empty client-provided names of the collections to use instead
   * of default to all the collection names from the database.
   * @return List of collection names which are either all of the collection names in the provided
   * 'mongoDatabase' or, if the provided 'collectionNames' is not empty, names of the collections
   * contained in the provided 'collectionNames'.
   * @throws IllegalArgumentException If at least one of the collection names in the provided
   * 'collectionNames' does not exist in the provided 'mongoDatabase'.
   */
  private static List<String> getCollectionNamesToUse(
      MongoDatabase mongoDatabase,
      String[] collectionNames) throws IllegalArgumentException {

    // Get names of all collections in db
    List<String> databaseCollectionNames = getCollectionNames(mongoDatabase);

    // If collectionNames is omitted, extract dataset from all collections
    List<String> collectionNamesToExtract = new ArrayList<>();
    if (collectionNames == null || collectionNames.length == 0) {

      collectionNamesToExtract.addAll(databaseCollectionNames);
    } else {

      // Loop over all provided collectionNames; check for existence and add one by one
      for (String collectionName : collectionNames) {

        if (databaseCollectionNames.contains(collectionName)) {

          collectionNamesToExtract.add(collectionName);
        } else {

          String message = "Specified collection '" + collectionName + "' does not exist in the"
              + " " + mongoDatabase.getName() + " database.";
          log.error(message);
          throw new IllegalArgumentException(message);
        }
      }
    }
    return collectionNamesToExtract;
  }

  /**
   * @param mongoDatabase Instance of the MongoDB database to extract all existing collection names
   * from.
   * @return List of existing collections in the provided 'mongoDatabase'.
   */
  private static List<String> getCollectionNames(MongoDatabase mongoDatabase) {

    List<String> collectionNames = new ArrayList<>();

    // Retrieve collection names from db
    for (String collectionName : mongoDatabase.listCollectionNames()) {
      collectionNames.add(collectionName);
    }

    return collectionNames;
  }

  /**
   * @param mongoCollection Mongo collection to extract all documents as a list of maps of field
   * name/value pairs.
   * @param mongoUnitProperties Collection of properties framework was configured with. If the
   * provided 'preserveBsonTypes' is null, this argument may also be 'null' and is ignored.
   * @param preserveBsonTypes List of string representation of {@link org.bson.BsonType} enum names
   * that should be preserved when creating documents. This **must** be 'null' when this method is
   * used for assertions instead of to output JSON through {@link DatasetGenerator}.
   * @return List of maps of field name/value pairs of all the documents in the provided
   * 'mongoCollection', where each map represents a single document.
   */
  private static List<Map<String, Object>> getMongoUnitDocuments(
      MongoCollection<Document> mongoCollection,
      MongoUnitProperties mongoUnitProperties,
      List<String> preserveBsonTypes) {

    List<Map<String, Object>> mongoUnitDocuments = new ArrayList<>();

    // Loop over each document in 'mongoCollection'
    FindIterable<Document> mongoDocuments = mongoCollection.find();
    for (Document document : mongoDocuments) {

      // Convert document to BSON document
      BsonDocument bsonDocument = document
          .toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());

      // Extract all mongo unit fields from this document and add them to document list as a map
      Map<String, Object> mongoUnitFields =
          getDocument(bsonDocument, mongoUnitProperties, preserveBsonTypes);
      mongoUnitDocuments.add(mongoUnitFields);
    }

    return mongoUnitDocuments;
  }

  /**
   * @param bsonDocument {@link BsonDocument} to extract all fields from.
   * @param mongoUnitProperties Collection of properties framework was configured with. If the
   * provided 'preserveBsonTypes' is null, this argument may also be 'null' and is ignored.
   * @param preserveBsonTypes List of string representation of {@link org.bson.BsonType} enum names
   * that should be preserved when creating documents. This **must** be 'null' when this method is
   * used for assertions instead of to output JSON through {@link DatasetGenerator}.
   * @return Map of field/value pairs that represent all the fields in the provided 'bsonDocument'.
   */
  private static Map<String, Object> getDocument(
      BsonDocument bsonDocument,
      MongoUnitProperties mongoUnitProperties,
      List<String> preserveBsonTypes) {

    Map<String, Object> document = new HashMap<>();

    // Loop over all document fields
    Set<String> fieldKeys = bsonDocument.keySet();
    for (String fieldKey : fieldKeys) {

      // Get value for field key
      BsonValue bsonValue = bsonDocument.get(fieldKey);
      Object mongoUnitField = getFieldValue(bsonValue, mongoUnitProperties, preserveBsonTypes);

      // Store field key and its value in the map
      document.put(fieldKey, mongoUnitField);
    }

    return document;
  }

  /**
   * @param bsonValue {@link BsonValue} to extract value from.
   * @param mongoUnitProperties Collection of properties framework was configured with. If the
   * provided 'preserveBsonTypes' is null, this argument may also be 'null' and is ignored.
   * @param preserveBsonTypes List of string representation of {@link org.bson.BsonType} enum names
   * that should be preserved when creating documents. This **must** be 'null' when this method is
   * used for assertions instead of to output JSON through {@link DatasetGenerator}.
   * @return Value that can be used for comparisons, i.e., simplified from its BsonType to simpler
   * types.
   */
  private static Object getFieldValue(
      BsonValue bsonValue,
      MongoUnitProperties mongoUnitProperties,
      List<String> preserveBsonTypes) {

    // Convert list of types to preserve to map for faster look up
    Map<String, String> preserveBsonTypesMap = preserveBsonTypes == null ?
        new HashMap<>() :
        preserveBsonTypes.stream().collect(Collectors.toMap(e -> e, e -> e));

    // Retrieve field name indicator if mongoUnitProperties is not null; otherwise set it to null
    String fieldNameIndicator = mongoUnitProperties == null ?
        null :
        mongoUnitProperties.getMongoUnitValueFieldNameIndicator();

    // Extract value based on the BsonType
    switch (bsonValue.getBsonType()) {

      case ARRAY:
        // Preserve ARRAY BSON type?
        if (preserveBsonTypesMap.containsKey("ARRAY")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "ARRAY",
              getArrayValues(bsonValue.asArray(), mongoUnitProperties, preserveBsonTypes));
        }

        return getArrayValues(bsonValue.asArray(), mongoUnitProperties, preserveBsonTypes);

      case DOCUMENT:
        // Preserve DOCUMENT BSON type?
        if (preserveBsonTypesMap.containsKey("DOCUMENT")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "DOCUMENT",
              getDocument(bsonValue.asDocument(), mongoUnitProperties, preserveBsonTypes));
        }

        return getDocument(bsonValue.asDocument(), mongoUnitProperties, preserveBsonTypes);

      case DOUBLE:
        // Preserve DOUBLE BSON type?
        if (preserveBsonTypesMap.containsKey("DOUBLE")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "DOUBLE",
              bsonValue.asDouble().getValue());
        }

        return bsonValue.asDouble().getValue();

      case STRING:
        // Preserve STRING BSON type?
        if (preserveBsonTypesMap.containsKey("STRING")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "STRING",
              bsonValue.asString().getValue());
        }

        return bsonValue.asString().getValue();

      case BINARY:
        // Preserve BINARY BSON type?
        if (preserveBsonTypesMap.containsKey("BINARY")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "BINARY",
              Base64.getEncoder().encodeToString(bsonValue.asBinary().getData()));
        }

        // Store using Base64 encoding
        return Base64.getEncoder().encodeToString(bsonValue.asBinary().getData());

      case OBJECT_ID:
        // Preserve OBJECT_ID BSON type?
        if (preserveBsonTypesMap.containsKey("OBJECT_ID")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "OBJECT_ID",
              bsonValue.asObjectId().getValue().toHexString());
        }

        return bsonValue.asObjectId().getValue().toHexString();

      case BOOLEAN:
        // Preserve BOOLEAN BSON type?
        if (preserveBsonTypesMap.containsKey("BOOLEAN")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "BOOLEAN",
              bsonValue.asBoolean().getValue());
        }

        return bsonValue.asBoolean().getValue();

      case DATE_TIME:
        // Preserve DATE_TIME BSON type?
        if (preserveBsonTypesMap.containsKey("DATE_TIME")) {

          SimpleDateFormat format = new SimpleDateFormat(DATE_STRING_FORMAT);
          String dateValue = format.format(new Date(bsonValue.asDateTime().getValue()));

          return generateMongoUnitValueDocument(fieldNameIndicator, "DATE_TIME", dateValue);
        }

        return bsonValue.asDateTime().getValue();

      case NULL:
        // Preserve NULL BSON type?
        if (preserveBsonTypesMap.containsKey("NULL")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "NULL",
              null);
        }

        return null;

      case UNDEFINED:
        // Preserve UNDEFINED BSON type?
        if (preserveBsonTypesMap.containsKey("UNDEFINED")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "UNDEFINED",
              null);
        }

        return null;

      case REGULAR_EXPRESSION:
        // Preserve REGULAR_EXPRESSION BSON type?
        if (preserveBsonTypesMap.containsKey("REGULAR_EXPRESSION")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "REGULAR_EXPRESSION",
              bsonValue.asRegularExpression().getPattern());
        }

        return bsonValue.asRegularExpression().getPattern();

      case DB_POINTER:
        String namespace = bsonValue.asDBPointer().getNamespace();
        String objectId = bsonValue.asObjectId().getValue().toHexString();

        Map<String, String> dbPointerValueMap = new HashMap<>();
        dbPointerValueMap.put("namespace", namespace);
        dbPointerValueMap.put("objectId", objectId);

        // Preserve DB_POINTER BSON type?
        if (preserveBsonTypesMap.containsKey("DB_POINTER")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "DB_POINTER",
              dbPointerValueMap);
        }

        return dbPointerValueMap;

      case JAVASCRIPT:
        // Preserve JAVASCRIPT BSON type?
        if (preserveBsonTypesMap.containsKey("JAVASCRIPT")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "JAVASCRIPT",
              bsonValue.asJavaScript().getCode());
        }

        return bsonValue.asJavaScript().getCode();

      case SYMBOL:
        // Preserve SYMBOL BSON type?
        if (preserveBsonTypesMap.containsKey("SYMBOL")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "SYMBOL",
              bsonValue.asSymbol().getSymbol());
        }

        return bsonValue.asSymbol().getSymbol();

      case JAVASCRIPT_WITH_SCOPE:
        // Preserve JAVASCRIPT_WITH_SCOPE BSON type?
        if (preserveBsonTypesMap.containsKey("JAVASCRIPT_WITH_SCOPE")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "JAVASCRIPT_WITH_SCOPE",
              bsonValue.asJavaScriptWithScope().getCode());
        }

        return bsonValue.asJavaScriptWithScope().getCode();

      case INT32:
        // Preserve INT32 BSON type?
        if (preserveBsonTypesMap.containsKey("INT32")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "INT32",
              bsonValue.asInt32().getValue());
        }

        return bsonValue.asInt32().getValue();

      case TIMESTAMP:
        // Preserve TIMESTAMP BSON type?
        if (preserveBsonTypesMap.containsKey("TIMESTAMP")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "TIMESTAMP",
              bsonValue.asTimestamp().getValue());
        }

        return bsonValue.asTimestamp().getValue();

      case INT64:
        // Preserve INT64 BSON type?
        if (preserveBsonTypesMap.containsKey("INT64")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "INT64",
              bsonValue.asInt64().getValue());
        }

        return bsonValue.asInt64().getValue();

      case DECIMAL128:
        // Preserve DECIMAL128 BSON type?
        if (preserveBsonTypesMap.containsKey("DECIMAL128")) {
          return generateMongoUnitValueDocument(
              fieldNameIndicator,
              "DECIMAL128",
              bsonValue.asDecimal128().decimal128Value().bigDecimalValue());
        }

        return bsonValue.asDecimal128().decimal128Value().bigDecimalValue();

      // END_OF_DOCUMENT, MIN_KEY, MAX_KEY
      default:
        String message = "BSON type " + bsonValue.getBsonType() + " is not currently supported by"
            + " the MongoUnit framework.";
        log.error(message);
        throw new MongoUnitException(message);
    }
  }

  /**
   * @param fieldNameIndicator Field name indicator that is configured to be a trigger to recognize
   * that the provided 'mongoUnitValueDocument' is using a special MongoUnit schema format.
   * @param bsonType String name of a BSON TYPE corresponding to the enum name of {@link
   * org.bson.BsonType}.
   * @param value The value to set for the 'value' part of the MongoUnit value document.
   * @return A special MongoUnit value document with a single name/value pair where the name is the
   * provided 'fieldNameIndicator' concatenated with the provided 'bsonType' and the value is the
   * provided 'value'.
   */
  public static Map<String, Object> generateMongoUnitValueDocument(
      String fieldNameIndicator,
      String bsonType,
      Object value) {

    Map<String, Object> mongoUnitValueDocument = new HashMap<>();
    String key = fieldNameIndicator + bsonType;
    mongoUnitValueDocument.put(key, value);

    return mongoUnitValueDocument;
  }

  /**
   * @param bsonArrayValue {@link BsonArray} which contains values to extract.
   * @param mongoUnitProperties Collection of properties framework was configured with.
   * @param preserveBsonTypes List of string representation of {@link org.bson.BsonType} enum names
   * that should be preserved when creating documents. This **must** be 'null' when this method is
   * used for assertions instead of to output JSON through {@link DatasetGenerator}.
   * @return List of values contained in the provided 'bsonArrayValue'.
   */
  private static List<Object> getArrayValues(
      BsonArray bsonArrayValue,
      MongoUnitProperties mongoUnitProperties,
      List<String> preserveBsonTypes) {

    List<Object> arrayValues = new ArrayList<>();

    // Loop over array values and extract each one
    for (BsonValue bsonValue : bsonArrayValue.getValues()) {

      // Extract value and add it to list of array values
      Object value = getFieldValue(bsonValue, mongoUnitProperties, preserveBsonTypes);
      arrayValues.add(value);
    }

    return arrayValues;
  }

  /**
   * @param mongoUnitDocuments List of maps of field name/value pairs of all the documents in this
   * collection, where each map represents a single document.
   * @param mongoUnitProperties Collection of properties framework was configured with.
   * @return List of MongoDB BSON {@link Document} objects ready to insert into database
   * @throws MongoUnitException If anything goes wrong with translating the provided
   * 'mongoUnitDocuments'.
   */
  private static List<Document> toBsonDocuments(
      List<Map<String, Object>> mongoUnitDocuments,
      MongoUnitProperties mongoUnitProperties) throws MongoUnitException {

    List<Document> bsonDocuments = new ArrayList<>();

    // Loop over all mongo unit documents
    for (int i = 0; i < mongoUnitDocuments.size(); i++) {

      Map<String, Object> document = mongoUnitDocuments.get(i);

      // Convert each mongo unit document to BSON document; add to collection of documents
      Document bsonDocument;
      try {

        bsonDocument = toBsonDocument(document, mongoUnitProperties);

      } catch (MongoUnitException mongoUnitException) {

        // Add tracing to the exception message
        String message = "Document array index of '" + i + "', document of " + document + " : ";
        throw new MongoUnitException(message + mongoUnitException, mongoUnitException);
      }

      bsonDocuments.add(bsonDocument);
    }

    return bsonDocuments;
  }

  /**
   * @param mongoUnitDocument Map of field name/value pairs of a single mongo unit document.
   * @param mongoUnitProperties Collection of properties framework was configured with.
   * @return MongoDB BSON {@link Document} representation of the provided 'mongoUnitDocument', ready
   * to be inserted into the database.
   * @throws MongoUnitException If anything goes wrong with translating the provided
   * 'mongoUnitDocument'.
   */
  private static Document toBsonDocument(
      Map<String, Object> mongoUnitDocument,
      MongoUnitProperties mongoUnitProperties) throws MongoUnitException {

    Document bsonDocument = new Document();

    // Extract each field from the map
    Set<String> fieldNames = mongoUnitDocument.keySet();
    for (String fieldName : fieldNames) {

      // Get the value
      Object mongoUnitFieldValue = mongoUnitDocument.get(fieldName);

      // Extract possibly different BSON value
      Object bsonFieldValue;
      try {

        bsonFieldValue = toBsonValue(mongoUnitFieldValue, mongoUnitProperties);

      } catch (MongoUnitException mongoUnitException) {

        // Add tracing to the exception message
        String message = "Field name '" + fieldName + "': ";
        throw new MongoUnitException(message + mongoUnitException.getMessage(), mongoUnitException);
      }

      // Add name/value pair to BSON document
      bsonDocument.append(fieldName, bsonFieldValue);
    }

    return bsonDocument;
  }

  /**
   * @param mongoUnitFieldValue Object that contains some value. Value can be in the MongoUnit
   * schema format, specifying 'bsonType' (by default, or some other configured field name), etc.,
   * or it can be a regular document, array, etc.
   * @param mongoUnitProperties Collection of properties framework was configured with.
   * @return An instance of {@link Object} that contains either the raw object that needs to be
   * stored in the {@link Document} as value or some BSON specific construct, depending on what the
   * provided 'mongoUnitFieldValue' holds.
   * @throws MongoUnitException If anything goes wrong with translating the provided
   * 'mongoUnitFieldValue'.
   */
  @SuppressWarnings("unchecked")
  private static Object toBsonValue(
      Object mongoUnitFieldValue,
      MongoUnitProperties mongoUnitProperties) throws MongoUnitException {

    // Check if value is a map
    if (mongoUnitFieldValue instanceof Map) {
      return toBsonValueAsMap((Map<String, Object>) mongoUnitFieldValue, mongoUnitProperties);
    }

    // Check if value is a list
    if (mongoUnitFieldValue instanceof List) {
      return toBsonValueAsList((List<Object>) mongoUnitFieldValue, mongoUnitProperties);
    }

    // Not a container value, so return as is
    return mongoUnitFieldValue;
  }

  /**
   * @param mongoUnitArrayFieldValue {@link List} of objects (array) that are to be attempted to be
   * extracted as List of BSON objects.
   * @param mongoUnitProperties Collection of properties framework was configured with.
   * @return List of potentially BSON objects extracted from the provided
   * 'mongoUnitArrayFieldValue'.
   * @throws MongoUnitException If anything goes wrong with translating the provided
   * 'mongoUnitArrayFieldValue'.
   */
  private static List<Object> toBsonValueAsList(
      List<Object> mongoUnitArrayFieldValue,
      MongoUnitProperties mongoUnitProperties) throws MongoUnitException {

    List<Object> array = new ArrayList<>();

    for (Object mongoUnitFieldValue : mongoUnitArrayFieldValue) {

      // Attempt to convert to BSON object
      Object bsonFieldValue = toBsonValue(mongoUnitFieldValue, mongoUnitProperties);
      array.add(bsonFieldValue);
    }

    return array;
  }

  /**
   * @param mongoUnitMapValue {@link Map} of values that can possibly be in the MongoUnit schema
   * value format, specifying 'bsonType' (by default, or some other configured field name), or some
   * other, etc., or it can be a regular document.
   * @param mongoUnitProperties Collection of properties framework was configured with.
   * @return An {@link Object} that either represents a regular BSON {@link Document} or a specific
   * {@link org.bson.BsonType} typed value.
   * @throws MongoUnitException If anything goes wrong with translating the provided
   * 'mongoUnitMapValue'.
   */
  private static Object toBsonValueAsMap(
      Map<String, Object> mongoUnitMapValue,
      MongoUnitProperties mongoUnitProperties) throws MongoUnitException {

    String fieldNameIndicator = mongoUnitProperties.getMongoUnitValueFieldNameIndicator();

    // Check if map is in special MongoUnit schema format
    if (isMongoUnitValue(mongoUnitMapValue, fieldNameIndicator)) {

      // Map is not a bson document but a MongoUnit schema to represent BSON typed single value
      return toBsonTypedValue(mongoUnitMapValue, mongoUnitProperties);
    }

    return toBsonDocument(mongoUnitMapValue, mongoUnitProperties);
  }

  /**
   * @param mongoUnitBsonTypedValue Map of values that represent a MongoUnit BSON typed value.
   * @param mongoUnitProperties Collection of properties framework was configured with.
   * @return An {@link Object} that represents a single {@link org.bson.BsonType} typed value,
   * correctly created as a BsonXXX instance.
   * @throws MongoUnitException If anything goes wrong with translating the provided
   * 'mongoUnitBsonTypedValue'.
   */
  @SuppressWarnings("unchecked")
  private static Object toBsonTypedValue(
      Map<String, Object> mongoUnitBsonTypedValue,
      MongoUnitProperties mongoUnitProperties) throws MongoUnitException {

    // Extract 'bsonType' and 'value' values out of the map
    String fieldNameIndicator = mongoUnitProperties.getMongoUnitValueFieldNameIndicator();
    MongoUnitValue mongoUnitValue =
        extractMongoUnitValue(mongoUnitBsonTypedValue, fieldNameIndicator);
    String bsonType = mongoUnitValue.getBsonType();
    Object value = mongoUnitValue.getValue();

    // Check that these field names are present
    if (bsonType == null || (bsonType.equals("NULL") && value == null)) {

      String message = "\"" + fieldNameIndicator + "BSON_TYPE\": value must be present in a"
          + " MongoUnit value type specification. 'value' can only be 'null' if 'BSON_TYPE' is"
          + " a string 'NULL' value.";
      log.error(message);
      throw new MongoUnitException(message);
    }

    try {
      // Extract value based on the BsonType - Strings match BsonType enum names
      switch (bsonType) {

        case "ARRAY":
          return toBsonValueAsList((List<Object>) value, mongoUnitProperties);

        case "DOCUMENT":
          return toBsonDocument((Map<String, Object>) value, mongoUnitProperties);

        case "DOUBLE":
          return new BsonDouble((double) value);

        case "STRING":
          return new BsonString((String) value);

        case "BINARY":
          // Decode using Base64 encoding
          return new BsonBinary(Base64.getDecoder().decode((String) value));

        case "OBJECT_ID":
          return new BsonObjectId(new ObjectId((String) value));

        case "BOOLEAN":
          return new BsonBoolean((boolean) value);

        case "DATE_TIME":

          try {

            SimpleDateFormat format = new SimpleDateFormat(DATE_STRING_FORMAT);
            Date date = format.parse((String) value);
            return new BsonDateTime(date.getTime());

          } catch (ParseException e) {
            String message = "Date value was not in the supported format of"
                + DATE_STRING_FORMAT + ". Tried to parse '" + value + "'.";
            log.error(message);
            throw new MongoUnitException(message);
          }

        case "NULL":
          return new BsonNull();

        case "UNDEFINED":
          return new BsonUndefined();

        case "REGULAR_EXPRESSION":
          return new BsonRegularExpression((String) value);

        case "DB_POINTER":
          // Use custom 'namespace' and 'objectId' as field names
          Map<String, String> dbPointerValueMap = (Map<String, String>) value;
          return new BsonDbPointer(
              dbPointerValueMap.get("namespace"),
              new ObjectId(dbPointerValueMap.get("objectId")));

        case "JAVASCRIPT":
        case "JAVASCRIPT_WITH_SCOPE":
          return new BsonJavaScript((String) value);

        case "SYMBOL":
          return new BsonSymbol((String) value);

        case "INT32":
          return new BsonInt32((int) value);

        case "TIMESTAMP":
          return new BsonTimestamp((long) value);

        case "INT64":
          return new BsonInt64((long) value);

        case "DECIMAL128":
          return new BsonDecimal128(new Decimal128(new BigDecimal((double) value)));

        // END_OF_DOCUMENT, MIN_KEY, MAX_KEY
        default:
          String message = "BSON type " + bsonType + " is not currently supported by"
              + " the MongoUnit framework.";
          log.error(message);
          throw new MongoUnitException(message);
      }
    } catch (MongoUnitException mongoUnitException) {

      // If MongoUnitException, rethrow it
      throw mongoUnitException;

    } catch (Exception exception) {

      String message = "Failed to treat value '" + value + "' of type '"
          + value.getClass().getName() + "' as the provided bsonType '" + bsonType + "'.";
      throw new MongoUnitException(message);

    }
  }

  /**
   * @param mongoUnitValueDocument Map which contains keys in the special MongoUnit value format.
   * @param fieldNameIndicator Field name indicator that is configured to be a trigger to recognize
   * that the provided 'mongoUnitValueDocument' is using a special MongoUnit schema format.
   * @return Instance of {@link MongoUnitValue} which contains actual value, BsonType, and
   * comparator.
   * @throws MongoUnitException If no keys in the provided 'mongoUnitValueDocument' contain the
   * special trigger indicator provided by 'fieldNameIndicator'.
   */
  public static MongoUnitValue extractMongoUnitValue(
      Map<String, Object> mongoUnitValueDocument,
      String fieldNameIndicator) throws MongoUnitException {

    // Extract all keys
    Set<String> allKeys = mongoUnitValueDocument.keySet();

    // Find key that contains field name indicator
    String indicatorKey = allKeys
        .stream()
        .filter(key -> key.startsWith(fieldNameIndicator))
        .findAny()
        .orElseThrow(() -> {
          String message = "Error: the following document was expected to have special"
              + " MongoUnit value format but didn't: '" + mongoUnitValueDocument + "'.";
          throw new MongoUnitException(message);
        });

    // Extract bson type; if not there, set it to null
    String bsonType = indicatorKey.substring(fieldNameIndicator.length());
    bsonType = bsonType.trim().length() == 0 ? null : bsonType;

    Object value = mongoUnitValueDocument.get(indicatorKey);
    String comparatorValue = (String) mongoUnitValueDocument.get(COMPARATOR_FIELD_NAME);

    return MongoUnitValue.builder()
        .bsonType(bsonType)
        .value(value)
        .comparatorValue(comparatorValue)
        .build();
  }

  /**
   * Returns An {@link AssertionResult} with a 'match' of 'true' if the provided 'expected' and
   * 'actual' lists of {@link MongoUnitCollection}s match according to the MongoUnit framework
   * rules, or with 'false' otherwise.
   *
   * Rules for matching:
   *
   * 1) Match is not effected if 'expected' is missing a field name in its definition.
   *
   * 2) Presence of a configurable special field name key ("$$" with the value to compare actual
   * data with) in a document, allows framework to look for another special field "comparator". Its
   * value can be either "=", ">", or "<". The "=" is how every field value compared by default if
   * the special document containing "comparator" is not present. "<" and ">" compare values to
   * ensure one is less than or greater than the other. These comparisons will work for Strings,
   * dates, date/time stamps, numbers (or any type that implements {@link Comparable} interface).
   *
   * @param expected List of {@link MongoUnitCollection}s that the provided 'actual' dataset is to
   * be compared against. An identical list is not necessarily to achieve a match and thus this list
   * may contain special fields that guide the matching process.
   * @param actual List of {@link MongoUnitCollection}s retrieved from the database after the target
   * test call.
   * @param mongoUnitProperties Collection of properties framework was configured with.
   * @return An {@link AssertionResult} with a 'match' of 'true' if the provided 'expected' and
   * 'actual' lists of {@link MongoUnitCollection}s match according to the MongoUnit framework
   * rules, or with 'false' otherwise.
   */
  public static AssertionResult assertMatches(
      List<MongoUnitCollection> expected,
      List<MongoUnitCollection> actual,
      MongoUnitProperties mongoUnitProperties) {

    // Assert the same number of collections
    if (expected.size() != actual.size()) {

      String message = "Expected " + expected.size() + " collections, but found " + actual.size()
          + ".";
      return new AssertionResult(false, message);
    }

    // Compile a map of actual mongo unit collections based on name
    Map<String, MongoUnitCollection> actualMap =
        actual.stream().collect(Collectors.toMap(MongoUnitCollection::getCollectionName, e -> e));

    // Loop over expected results and match with actual
    for (MongoUnitCollection expectedMongoUnitCollection : expected) {

      String expectedCollectionName = expectedMongoUnitCollection.getCollectionName();
      MongoUnitCollection actualMongoUnitCollection =
          actualMap.get(expectedCollectionName);

      // Assert such a collection is present in the actual
      if (actualMongoUnitCollection == null) {

        String message = "Expected collection " + expectedCollectionName + " to be present.";
        return new AssertionResult(false, message);
      }

      // Assert this collection matches; if doesn't match, return immediately
      AssertionResult singleCollectionAssertionResult;
      try {

        singleCollectionAssertionResult = assertMatches(
            expectedMongoUnitCollection,
            actualMongoUnitCollection,
            mongoUnitProperties);

      } catch (MongoUnitException mongoUnitException) {

        // Add tracing to the exception message
        String message = "Collection '" + expectedCollectionName + "': ";
        throw new MongoUnitException(message + mongoUnitException.getMessage(), mongoUnitException);
      }

      // Return immediately if assertion failed
      if (!singleCollectionAssertionResult.isMatch()) {
        String message = "Collection '" + expectedCollectionName + "': "
            + singleCollectionAssertionResult.getMessage();
        return new AssertionResult(false, message);
      }
    }

    return new AssertionResult(true, "Database state matches.");
  }

  /**
   * Returns An {@link AssertionResult} with a 'match' of 'true'  if the provided 'expected' and
   * 'actual' {@link MongoUnitCollection}s match according to the MongoUnit framework rules, or with
   * 'false' otherwise.
   *
   * Rules for matching:
   *
   * 1) Match is not effected if 'expected' is missing a field name in its definition.
   *
   * 2) Presence of a configurable special field name key ("$$" with the value to compare actual
   * data with) in a document, allows framework to look for another special field "comparator". Its
   * value can be either "=", ">", or "<". The "=" is how every field value compared by default if
   * the special document containing "comparator" is not present. "<" and ">" compare values to
   * ensure one is less than or greater than the other. These comparisons will work for Strings,
   * dates, date/time stamps, numbers (or any type that implements {@link Comparable} interface).
   *
   * @param expected {@link MongoUnitCollection}s that the provided 'actual' dataset is to be
   * compared against. An identical list is not necessarily to achieve a match and thus this list
   * may contain special fields that guide the matching process.
   * @param actual {@link MongoUnitCollection}s retrieved from the database after the target test
   * call.
   * @param mongoUnitProperties Collection of properties framework was configured with.
   * @return An {@link AssertionResult} with a 'match' of 'true'  if the provided 'expected' and
   * 'actual' {@link MongoUnitCollection}s match according to the MongoUnit framework rules, or with
   * 'false' otherwise.
   * @throws MongoUnitException If the expected collection name is null.
   */
  public static AssertionResult assertMatches(
      MongoUnitCollection expected,
      MongoUnitCollection actual,
      MongoUnitProperties mongoUnitProperties) throws MongoUnitException {

    // Verify expected collection name is not null
    if (expected.getCollectionName() == null) {
      throw new MongoUnitException("Expected collection name can not be 'null'.");
    }

    // Assert collection names match
    if (!expected.getCollectionName().equals(actual.getCollectionName())) {

      String message = "Expected collection with name '" + expected.getCollectionName() + "' but"
          + " got '" + actual.getCollectionName() + "'.";
      return new AssertionResult(false, message);
    }

    List<Map<String, Object>> expectedDocuments = expected.getDocuments();
    List<Map<String, Object>> actualDocuments = actual.getDocuments();

    // Assert number of documents match
    if (expectedDocuments.size() != actualDocuments.size()) {

      String message = "Expected " + expectedDocuments.size() + " documents in collection '"
          + expected.getCollectionName() + "' but got " + actualDocuments.size();
      return new AssertionResult(false, message);
    }

    // Run through expected documents and match with corresponding actual document
    for (int i = 0; i < expectedDocuments.size(); i++) {

      // Get same indexed expected and actual documents
      Map<String, Object> expectedDocument = expectedDocuments.get(i);
      Map<String, Object> actualDocument = actualDocuments.get(i);

      // Assert single document matches
      AssertionResult singleDocumentAssertionResult;
      try {

        singleDocumentAssertionResult =
            assertMatches(expectedDocument, actualDocument, mongoUnitProperties);

      } catch (MongoUnitException mongoUnitException) {

        // Add tracing information
        String message = "Document array index of '" + i + "', expected document of "
            + expectedDocument + " : ";
        throw new MongoUnitException(message + mongoUnitException.getMessage(), mongoUnitException);
      }

      // Return immediately if assertion failed
      if (!singleDocumentAssertionResult.isMatch()) {
        String message = "Document '" + actualDocument + "': "
            + singleDocumentAssertionResult.getMessage();
        return new AssertionResult(false, message);
      }
    }

    return new AssertionResult(true, "Collections match.");
  }

  /**
   * Returns an {@link AssertionResult} with a 'match' of 'true'  if the provided 'expectedDocument'
   * and 'actualDocument' match according to the MongoUnit framework rules, or with 'false'
   * otherwise.
   *
   * Fields not included in the provided 'expectedDocument' are assumed irrelevant to the match.
   *
   * @param expectedDocument {@link Map} of field names with values that represent the expected
   * document.
   * @param actualDocument {@link Map} of field names with values that represents actual document in
   * the database.
   * @param mongoUnitProperties Collection of properties framework was configured with.
   * @return An {@link AssertionResult} with a 'match' of 'true'  if the provided 'expectedDocument'
   * and 'actualDocument' match according to the MongoUnit framework rules, or with 'false'
   * otherwise.
   * @throws MongoUnitException If anything goes wrong with processing this assertion.
   */
  public static AssertionResult assertMatches(
      Map<String, Object> expectedDocument,
      Map<String, Object> actualDocument,
      MongoUnitProperties mongoUnitProperties) throws MongoUnitException {

    // Loop through all expected field names and check for match in actual
    Set<String> expectedFieldNames = expectedDocument.keySet();
    for (String expectedFieldName : expectedFieldNames) {

      // Assert field with the same exists in actual
      Object actualValue = actualDocument.get(expectedFieldName);
      if (actualValue == null) {

        String message = "Expected field name '" + expectedFieldName + "' to be present.";
        return new AssertionResult(false, message);
      }

      Object expectedValue = expectedDocument.get(expectedFieldName);

      // Assert values match
      AssertionResult singleValueAssertionResult;
      try {

        singleValueAssertionResult =
            assertMatchesValue(expectedValue, actualValue, mongoUnitProperties);

      } catch (MongoUnitException mongoUnitException) {

        // Add tracing information
        String message = "Field name '" + expectedFieldName + "': ";
        throw new MongoUnitException(message + mongoUnitException.getMessage(), mongoUnitException);
      }

      // Return immediately if assertion failed
      if (!singleValueAssertionResult.isMatch()) {
        String message = "Field name '" + expectedFieldName + "': "
            + singleValueAssertionResult.getMessage();
        return new AssertionResult(false, message);
      }
    }

    return new AssertionResult(true, "Documents match.");
  }

  /**
   * @param expectedValue Expected value.
   * @param actualValue Actual value retrieved from the database.
   * @param mongoUnitProperties Collection of properties framework was configured with.
   * @return An {@link AssertionResult} with a 'match' of 'true'  if the provided 'expectedValue'
   * and 'actualValue' match according to the MongoUnit framework rules, or with 'false' otherwise.
   * @throws MongoUnitException If anything goes wrong with processing this assertion.
   */
  @SuppressWarnings("unchecked")
  public static AssertionResult assertMatchesValue(
      Object expectedValue,
      Object actualValue,
      MongoUnitProperties mongoUnitProperties) throws MongoUnitException {

    // Determine if expected value is a document
    if (expectedValue instanceof Map) {

      // Determine if expected value is a special MongoUnit document
      String fieldNameIndicator = mongoUnitProperties.getMongoUnitValueFieldNameIndicator();
      if (isMongoUnitValue((Map<String, Object>) expectedValue, fieldNameIndicator)) {

        // Assert match using specialized MongoUnit value comparator
        return assertMatchesMongoUnitValue(
            (Map<String, Object>) expectedValue,
            actualValue,
            fieldNameIndicator);

      } else {

        // Assert actual value is also a document
        if (!(actualValue instanceof Map)) {
          String message = "Expected a document but got '" + actualValue + "'.";
          return new AssertionResult(false, message);
        }

        // Assert match as a regular document
        return assertMatches(
            (Map<String, Object>) expectedValue,
            (Map<String, Object>) actualValue,
            mongoUnitProperties);
      }

    } else if (expectedValue instanceof List) {

      // Assert actual value is also a list
      if (!(actualValue instanceof List)) {
        String message = "Expected an array but got '" + actualValue + "'.";
        return new AssertionResult(false, message);
      }

      // Assert lists match
      return assertMatch(
          (List) expectedValue,
          (List) actualValue,
          mongoUnitProperties);

    } else { // Anything other than Map or List

      // Try to cast expected
      Comparable comparableExpected = expectedToComparable(expectedValue, null);

      // Assert that actual is also not a Map or a List; if not, cast to Comparable
      if (actualValue instanceof Map || actualValue instanceof List) {
        String message = "Expected '" + expectedValue + "' but got '" + actualValue + "'.";
        return new AssertionResult(false, message);
      }

      Comparable comparableActual = actualToComparable(actualValue);

      // Compare expected and actual
      int comparison = compare(comparableExpected, comparableActual);

      // Assert values match
      if (comparison != 0) {
        String message = "Expected '" + comparableExpected + "' to be equal to '"
            + comparableActual + "'";
        return new AssertionResult(false, message);
      }

      return new AssertionResult(true, "Values match.");
    }
  }

  /**
   * @param expectedList List of values expected.
   * @param actualList List of actual values.
   * @param mongoUnitProperties Collection of properties framework was configured with.
   * @return An {@link AssertionResult} with a 'match' of 'true' if the provided 'expectedList' and
   * 'actualList' match according to the MongoUnit framework rules, or with 'false' otherwise.
   */
  private static AssertionResult assertMatch(
      List expectedList,
      List actualList,
      MongoUnitProperties mongoUnitProperties) {

    // Assert lists are the same size
    if (expectedList.size() != actualList.size()) {

      String message = "Expected array size of '" + expectedList.size() + "' but got '"
          + actualList.size() + "'.";
      return new AssertionResult(false, message);
    }

    // Loop over expected list and assert match in actual list
    for (int i = 0; i < expectedList.size(); i++) {

      Object expectedValue = expectedList.get(i);
      Object actualValue = actualList.get(i);
      AssertionResult singleListValueAssertionResult =
          assertMatchesValue(expectedValue, actualValue, mongoUnitProperties);

      if (!singleListValueAssertionResult.isMatch()) {
        return singleListValueAssertionResult;
      }
    }

    return new AssertionResult(true, "Arrays match.");
  }

  /**
   * Returns {@link AssertionResult} with a 'match' of 'true'  if the provided 'expectedValue' and
   * 'actualValue' match according to the MongoUnit framework rules, or with 'false' otherwise.
   *
   * The value of "comparator" can be either "=", ">", "<", ">=", "<=". The "=" is how every field
   * value compared by default if the special document containing "comparator" is not present. "<"
   * and ">" compare values to ensure one is less than or greater than the other. The ">=" and "<="
   * compare values to ensure one is less than or greater than or equal to the other.
   *
   * If the "comparator" field is not specified, it's assumed to be "=".
   *
   * ">" assertion is read: is expected greater than actual, i.e., expected > actual. "<" assertion
   * is read: is expected less than actual, i.e., expected < actual, etc.
   *
   * These comparisons will ONLY work for strings, dates, date/time stamps, numbers (or any type
   * that implements {@link Comparable} interface).
   *
   * @param expectedMongoUnitValue Expected value expressed as a special MongoUnit value document,
   * including the "comparator" field which dictates how its "value" field value should be compared
   * to the provided 'actualValue'.
   * @param actualValue Actual value extracted from the database.
   * @param fieldNameIndicator Field name indicator that is configured to be a trigger to recognize
   * that the provided 'value' is using a special MongoUnit schema format.
   * @return An {@link AssertionResult} with a 'match' of 'true'  if the provided 'expectedValue'
   * and 'actualValue' match according to the MongoUnit framework rules, or with 'false' otherwise.
   * @throws MongoUnitException If the developer specified expected value appears not to be of type
   * {@link Comparable} and therefore not supported.
   */
  public static AssertionResult assertMatchesMongoUnitValue(
      Map<String, Object> expectedMongoUnitValue,
      Object actualValue,
      String fieldNameIndicator) throws MongoUnitException {

    // Extract MongoUnit values
    MongoUnitValue mongoUnitValue =
        extractMongoUnitValue(expectedMongoUnitValue, fieldNameIndicator);
    String comparator = mongoUnitValue.getComparatorValue();
    String bsonType = mongoUnitValue.getBsonType();
    Object expectedValue = mongoUnitValue.getValue();

    // Assume "=" if 'comparator' is null
    if (comparator == null) {
      comparator = "=";
    }

    // Throw exception if expected is null and comparator is not either "=" or "!="
    if (expectedValue == null && !comparator.equals("=") && !comparator.equals("!=")) {
      String message = "If expected value is specified as 'null', comparator must either be '=' or"
          + " '!='.";
      log.error(message);
      throw new MongoUnitException(message);
    }

    // Try to cast expected & actual values to Comparable
    Comparable comparableExpected = expectedToComparable(expectedValue, bsonType);
    Comparable comparableActual = actualToComparable(actualValue);

    // Compare expected and actual
    int comparison = compare(comparableExpected, comparableActual);

    // Assert depending on the 'comparator' value set by developer
    switch (comparator) {

      case "=":

        // Prep failed assertion message
        String message = "Expected '" + comparableExpected + "' but got '" + comparableActual
            + "'.";

        if (comparison != 0) {
          return new AssertionResult(false, message);
        } else {
          return new AssertionResult(true, "Values match.");
        }

      case "!=":

        // Prep failed assertion message
        message = "Expected '" + comparableExpected + "' to be not equal to actual value but got '"
            + comparableActual + "'.";

        if (comparison == 0) {
          return new AssertionResult(false, message);
        } else {
          return new AssertionResult(true, "Values are not equal as expected.");
        }

      case "<":

        // Prep failed assertion message
        message = "Expected '" + comparableExpected + "' to be less than actual but got '"
            + comparableActual + "' as actual.";

        if (comparison >= 0) {
          return new AssertionResult(false, message);
        } else {
          return new AssertionResult(true, "Expected is less than actual as expected.");
        }

      case "<=":

        // Prep failed assertion message
        message = "Expected '" + comparableExpected + "' to be less than or equal to actual but "
            + "got '" + comparableActual + "' as actual.";

        if (comparison > 0) {
          return new AssertionResult(false, message);
        } else {
          return new AssertionResult(true, "Expected is less than or equal to actual as expected.");
        }

      case ">":

        // Prep failed assertion message
        message = "Expected '" + comparableExpected + "' to be greater than actual but got '"
            + comparableActual + "' as actual.";

        if (comparison <= 0) {
          return new AssertionResult(false, message);
        } else {
          return new AssertionResult(true, "Expected is greater than actual as expected.");
        }

      case ">=":

        // Prep failed assertion message
        message = "Expected '" + comparableExpected + "' to be greater than or equal to actual "
            + "but got '" + comparableActual + "' as actual.";

        if (comparison < 0) {
          return new AssertionResult(false, message);
        } else {
          return
              new AssertionResult(true, "Expected is greater than or equal to actual as expected.");
        }

      default:
        // Unsupported value provided for comparator
        message = "Error: " + COMPARATOR_FIELD_NAME + " value of '" + comparator
            + "' is not supported.";

        throw new MongoUnitException(message);
    }
  }

  /**
   * @param expected Expected value to compare.
   * @param actual Actual value to compare.
   * @return A negative integer, zero, or a positive integer as the expected is less than, equal to,
   * or greater than the actual.
   */
  @SuppressWarnings("unchecked")
  public static int compare(Comparable expected, Comparable actual) {

    // Treat expected = null, actual != null as expected < actual
    if (expected == null && actual != null) {
      return -1;
    }

    // Treat expected != null, actual = null as expected > actual
    if (expected != null && actual == null) {
      return 1;
    }

    // Compare raw values for equality
    if (expected == actual) {
      return 0;
    }

    return expected.compareTo(actual);
  }

  /**
   * @param actualValue Actual value to cast to {@link Comparable} type.
   * @return The originally provided 'actualValue' by as a {@link Comparable} type.
   * @throws MongoUnitException If such a cast is not possible due to the underlying type of the
   * provided 'actualValue'.
   */
  private static Comparable actualToComparable(Object actualValue)
      throws MongoUnitException {

    try {

      return (Comparable) actualValue;

    } catch (ClassCastException exception) {

      String message = "Actual value of '" + actualValue + "' does not appears to be supported"
          + " as Comparable. Its type appears to be '" + actualValue.getClass().getTypeName() + "'";
      log.error(message);
      throw new MongoUnitException(message, exception);
    }
  }

  /**
   * @param expectedValue Expected value to cast to {@link Comparable} type.
   * @param bsonType String representation of the {@link org.bson.BsonType} enum label.
   * @return The originally provided 'expectedValue' but as a {@link Comparable} type.
   * @throws MongoUnitException If such a cast is not possible due to the underlying type of the
   * provided 'expectedValue'.
   */
  private static Comparable expectedToComparable(Object expectedValue, String bsonType)
      throws MongoUnitException {

    try {

      // If bsonType isn't specified, just need to cast to Comparable
      if (bsonType == null) {
        return (Comparable) expectedValue;
      }

      switch (bsonType.trim()) {

        // Cases where return is as is because it's already Comparable
        case "":
        case "DOUBLE":
        case "STRING":
        case "OBJECT_ID":
        case "BOOLEAN":
        case "BINARY":
        case "NULL":
        case "UNDEFINED":
        case "REGULAR_EXPRESSION":
        case "JAVASCRIPT":
        case "SYMBOL":
        case "JAVASCRIPT_WITH_SCOPE":
        case "INT32":
        case "TIMESTAMP":
        case "INT64":
        case "DECIMAL128":

          return (Comparable) expectedValue;

        case "DATE_TIME":

          try {

            SimpleDateFormat format = new SimpleDateFormat(DATE_STRING_FORMAT);
            Date date = format.parse((String) expectedValue);
            return date.getTime();

          } catch (ParseException e) {
            String message = "Date value was not in the supported format of "
                + DATE_STRING_FORMAT + ". Tried to parse '" + expectedValue + "'.";
            log.error(message);
            throw new MongoUnitException(message);
          }

          // END_OF_DOCUMENT, MIN_KEY, MAX_KEY, DB_POINTER:
        default:
          String message = "BSON type " + bsonType + " is not currently supported by"
              + " the MongoUnit framework.";
          log.error(message);
          throw new MongoUnitException(message);
      }

    } catch (ClassCastException exception) {

      String message =
          "Expected value of '" + expectedValue + "' does not appear to be supported as"
              + " Comparable. Its type appears to be '" + expectedValue.getClass().getTypeName()
              + "'.";

      //noinspection ConstantConditions (IntelliJ inspection bug)
      if (bsonType != null && !bsonType.equals("")) {
        message += " Expected value's BSON type was specified to be '" + bsonType + "'.";
      }

      log.error(message);
      throw new MongoUnitException(message, exception);
    }
  }

  /**
   * @param value A {@link Map} which represents a document to check if it's a special document
   * representing a MongoUnit value (with special fields).
   * @param fieldNameIndicator Field name indicator that is configured to be a trigger to recognize
   * that the provided 'value' is using a special MongoUnit schema format.
   * @return 'true' if the provided 'value' is an instance of a special MongoUnit schema format,
   * 'false' otherwise.
   */
  private static boolean isMongoUnitValue(
      Map<String, Object> value,
      String fieldNameIndicator) {

    // Get all keys of the value map
    Set<String> allKeys = value.keySet();

    // Loop through keys and see if any of them start with the fieldNameIndicator
    for (String key : allKeys) {
      if (key.startsWith(fieldNameIndicator)) {
        return true;
      }
    }

    // Looked through all the keys and didn't find special field name indicator
    return false;
  }

  /**
   * Extracts {@link SeedWithDataset} and {@link AssertMatchesDataset} annotations from the provided
   * class or method, depending on the provided 'classLevelAnnotation' flag.
   *
   * @param context Test execution context which contains information about the target test class
   * and method.
   * @param classLevel If 'true', this method will extract class level annotations, if 'false',
   * method level annotations.
   * @return Instance of the {@link MongoUnitAnnotations} which contains lists of {@link
   * SeedWithDataset} and {@link AssertMatchesDataset} annotations in the order in which they
   * appeared on the annotated target.
   * @throws MongoUnitException If at least one {@link AssertMatchesDataset} annotation appears
   * before any of the {@link SeedWithDataset} annotations.
   */
  private static MongoUnitAnnotations extractAnnotations(
      ExtensionContext context,
      boolean classLevel) throws MongoUnitException {

    MongoUnitAnnotations mongoUnitAnnotations = new MongoUnitAnnotations();
    String errorMessage = "Error: No @AssertMatchesDataset(s) annotations can appear above any of"
        + " the @SeedWithDataset(s) annotations on a single element.";

    // Retrieve all explicitly declared annotations
    Annotation[] allAnnotations;
    if (classLevel) {
      allAnnotations = context.getRequiredTestClass().getDeclaredAnnotations();
    } else {
      allAnnotations = context.getRequiredTestMethod().getDeclaredAnnotations();
    }

    // Loop through all the annotations
    boolean assertMatchesDatasetAnnotationListStarted = false;
    for (Annotation annotation : allAnnotations) {

      if (annotation instanceof SeedWithDataset) {

        if (assertMatchesDatasetAnnotationListStarted) {
          log.error(errorMessage);
          throw new MongoUnitException(errorMessage);
        }

        mongoUnitAnnotations.addSeedWithDatasetAnnotation((SeedWithDataset) annotation);

      } else if (annotation instanceof SeedWithDatasets) {

        if (assertMatchesDatasetAnnotationListStarted) {
          log.error(errorMessage);
          throw new MongoUnitException(errorMessage);
        }

        mongoUnitAnnotations.addSeedWithDatasetAnnotations(((SeedWithDatasets) annotation).value());

      } else if (annotation instanceof AssertMatchesDataset) {

        assertMatchesDatasetAnnotationListStarted = true;
        mongoUnitAnnotations.addAssertMatchesDatasetAnnotation((AssertMatchesDataset) annotation);

      } else if (annotation instanceof AssertMatchesDatasets) {

        assertMatchesDatasetAnnotationListStarted = true;
        mongoUnitAnnotations
            .addAssertMatchesDatasetAnnotations(((AssertMatchesDatasets) annotation).value());
      }
    }

    return mongoUnitAnnotations;
  }

  /**
   * @param testClass Class instance of the test class.
   * @return Name of the test class. If specified by the 'name' attribute of {@link MongoUnitTest}
   * annotation. If not specified, it defaults to the simple class name of the testing class.
   * @throws MongoUnitException If the provided 'testClass' does not have {@link MongoUnitTest}
   * annotation on it.
   */
  public static String extractTestClassName(Class<?> testClass) throws MongoUnitException {

    // Throw exception if annotation is not present on test class
    if (!testClass.isAnnotationPresent(MongoUnitTest.class)) {
      String message = "@MongoUnitTest annotation was not found on '" + testClass.getName() + "'"
          + " class.";
      throw new MongoUnitException(message);
    }

    MongoUnitTest mongoUnitTest = testClass.getAnnotation(MongoUnitTest.class);

    // If name is an empty string, use the simple class name
    String mongoUnitTestAnnotationName = mongoUnitTest.name();
    if ("".equals(mongoUnitTestAnnotationName)) {
      return testClass.getSimpleName();
    }

    return mongoUnitTestAnnotationName;
  }

  /**
   * Extracts MongoUnit datasets based on the potential class or method level MongoUnit annotations.
   * The seed and assert datasets returned do not have same-named collections in the list of
   * collections.
   *
   * @param context Extension context within which this method is being executed.
   * @param testClassName Name of the test class, which is either {@link MongoUnitTest} specified
   * name or, if not specified, the simple class name of the test class.
   * @param classLevel If 'true', this method will treat this extraction on a class level, if
   * 'false', on a method level.
   * @return Instance of {@link MongoUnitDatasets} which potentially contains datasets to use for
   * seeding the database as well as asserting a match against. The seed and assert datasets
   * returned do not have same-named collections in the list of collections.
   * @throws MongoUnitException If at least one {@link AssertMatchesDataset} annotation appears
   * before any of the {@link SeedWithDataset} annotations or one of the annotations contains values
   * for mutually exclusive properties.
   */
  public static MongoUnitDatasets extractMongoUnitDatasets(
      ExtensionContext context,
      String testClassName,
      boolean classLevel) throws MongoUnitException {

    // Extract ordered class annotations
    MongoUnitAnnotations annotations = extractAnnotations(context, classLevel);

    MongoUnitDatasets mongoUnitDatasets = new MongoUnitDatasets();

    // If at least 1 assert annotation is present, remember its presence
    if (annotations.getAssertMatchesDatasetAnnotations().size() > 0) {
      mongoUnitDatasets.setAssertAnnotationPresent(true);
    }

    List<MongoUnitCollection> totalUncombinedSeedDataset = new ArrayList<>();
    List<MongoUnitCollection> totalUncombinedAssertDataset = new ArrayList<>();

    // Process seed annotations
    for (SeedWithDataset seedWithDatasetAnnotation : annotations.getSeedWithDatasetAnnotations()) {

      List<MongoUnitCollection> seedWithDataset =
          processSeedWithDatasetAnnotation(
              seedWithDatasetAnnotation,
              context,
              testClassName,
              classLevel);
      totalUncombinedSeedDataset.addAll(seedWithDataset);

      // If this is to be reused as assertion dataset, add to assertion list
      if (seedWithDatasetAnnotation.reuseForAssertion()) {
        totalUncombinedAssertDataset.addAll(seedWithDataset);
      }
    }

    // Process assert annotations
    for (AssertMatchesDataset assertMatchesDatasetAnnotation :
        annotations.getAssertMatchesDatasetAnnotations()) {

      List<MongoUnitCollection> assertMatchesDataset =
          processAssertMatchesDatasetAnnotation(
              assertMatchesDatasetAnnotation,
              context,
              testClassName,
              classLevel);
      totalUncombinedAssertDataset.addAll(assertMatchesDataset);
    }

    // Combine collections for optimization
    List<MongoUnitCollection> combinedSeedDataset = combineNoRepeatingCollections(
        totalUncombinedSeedDataset);
    List<MongoUnitCollection> combinedAssertDataset = combineNoRepeatingCollections(
        totalUncombinedAssertDataset);
    mongoUnitDatasets.setSeedWithDatasets(combinedSeedDataset);
    mongoUnitDatasets.setAssertMatchesDatasets(combinedAssertDataset);

    return mongoUnitDatasets;
  }

  /**
   * @param datasetWithRepeatingCollections List of {@link MongoUnitCollection}s that may have the
   * same collection repeated. Allowed to be 'null'.
   * @return List of {@link MongoUnitCollection}s where each collection does not repeat in the list
   * while preserving the original order of documents. If the provided
   * 'datasetWithRepeatingCollections' is 'null', an empty list is returned.
   */
  public static List<MongoUnitCollection> combineNoRepeatingCollections(
      List<MongoUnitCollection> datasetWithRepeatingCollections) {

    // Create a map of existing combined data by collection name
    Map<String, MongoUnitCollection> combinedDatasetMap = new HashMap<>();

    List<MongoUnitCollection> combinedDataset = new ArrayList<>();

    // If datasetWithRepeatingCollections is null, return empty list
    if (datasetWithRepeatingCollections == null) {
      return combinedDataset;
    }

    // Loop over uncombined dataset
    for (MongoUnitCollection collection : datasetWithRepeatingCollections) {

      // Attempt to retrieve same-named collection from map
      MongoUnitCollection existingCollection =
          combinedDatasetMap.get(collection.getCollectionName());

      // If collection doesn't exist in map yet, add it to the map keyed by its name
      if (existingCollection == null) {

        combinedDataset.add(collection);
        combinedDatasetMap.put(collection.getCollectionName(), collection);

      } else {

        existingCollection.getDocuments().addAll(collection.getDocuments());

      }
    }

    return combinedDataset;
  }

  /**
   * @param dataset1 List of {@link MongoUnitCollection} representing first dataset. Can't be null.
   * @param dataset2 List of {@link MongoUnitCollection} representing second dataset. Can't be
   * null.
   * @return Single *new* list of {@link MongoUnitCollection}s that contains only 1 list of
   * documents per collection with the preserved order of data from 'dataset1' first, 'dataset2'
   * second.
   */
  public static List<MongoUnitCollection> combineDatasets(
      List<MongoUnitCollection> dataset1,
      List<MongoUnitCollection> dataset2) {

    // Create a new list
    List<MongoUnitCollection> combinedDataset = new ArrayList<>();

    // Add both datasets into the combinedDataset
    combinedDataset.addAll(dataset1);
    combinedDataset.addAll(dataset2);

    return combineNoRepeatingCollections(combinedDataset);
  }

  /**
   * @param location Path to the file.
   * @param locationType Type of location the provided 'location' is.
   * @param relativePackageClass If 'locationType' is 'PACKAGE_PLUS_CLASS', this is the class type
   * whose package and class name (or name of {@link MongoUnitTest}) should be used for relativity
   * of the provided 'location' path. Otherwise, it's ignored and can be null.
   * @param testClassName Name of the test class, which is either {@link MongoUnitTest} specified
   * name or, if not specified, the simple class name of the test class.
   * @return Contents of the file pointed to by the provided 'location', given the provided
   * 'locationType'.
   * @throws MongoUnitException If anything goes wrong loading the dataset from the provided
   * 'location'.
   */
  public static String retrieveResourceFromFile(
      String location,
      LocationType locationType,
      Class<?> relativePackageClass,
      String testClassName) throws MongoUnitException {

    String resourceContents = null;

    try {

      switch (locationType) {

        case CLASSPATH_ROOT:

          // Check if location starts with "/" and, if not, add it
          if (location.charAt(0) != '/') {
            location = "/" + location;
          }

          Path path = Paths.get(MongoUnitUtil.class.getResource(location).toURI());
          resourceContents = Files.readString(path);

          break;

        case PACKAGE_PLUS_CLASS:

          // If relativePackageClass is not present, throw exception
          if (relativePackageClass == null) {
            String message = "Specified location of '" + location + "' with location type of "
                + "'PACKAGE_PLUS_CLASS' must also specify a non-null class to whose package and "
                + "name (or name specified in @MongoUnitTest this location is relative to.";
            throw new MongoUnitException(message);
          }

          // Make sure path always starts with "/" because it will be prepended with test class name
          if (location.charAt(0) != '/') {
            location = "/" + location;
          }

          // Add test class name to the location
          location = testClassName + location;

          path = Paths.get(relativePackageClass.getResource(location).toURI());
          resourceContents = Files.readString(path);

          break;

        case ABSOLUTE:

          resourceContents = Files.readString(Paths.get(location));

          break;

      }
    } catch (Exception exception) {

      String testClassNamePath = getTestClassNamePath(relativePackageClass);

      String testClassRelativeMessage = locationType == LocationType.PACKAGE_PLUS_CLASS ?
          " Attempted '" + testClassNamePath + "/" + location + "'." :
          "";

      String message = "Failed to load file resource at location '" + location + "', "
          + "with locationType of '" + locationType + "'." + testClassRelativeMessage;
      throw new MongoUnitException(message, exception);
    }

    return resourceContents;
  }

  /**
   * @param packagedClass Class based on whose package the returned path is constructed. Can be
   * 'null'.
   * @return 'null' if the provided 'relativePackageClass' is 'null', otherwise, a string that
   * consists of a leading '/' followed by 'relativePackageClass' package name converted into a path
   * combined with trailing '/'.
   */
  public static String getTestClassNamePath(Class<?> packagedClass) {

    // Return "null" if packagedClass is null
    if (packagedClass == null) {
      return "null";
    }

    return "/" + packagedClass.getPackageName().replace(".", "/");
  }

  /**
   * Returns List of {@link MongoUnitCollection}s based on the data pointed to by the 'value' or
   * 'locations' (or standard location).
   *
   * NOTE: The return does not combine documents from same-named collections into a single list of
   * documents under the same collection.
   *
   * @param annotation Instance of the {@link SeedWithDataset} annotation.
   * @param context Test execution context within which the test is being executed.
   * @param testClassName Name of the test class, which is either {@link MongoUnitTest} specified
   * name or, if not specified, the simple class name of the test class.
   * @param classLevel Flag which if set to 'true', indicates that this annotation was placed on a
   * class as opposed to method.
   * @return List of {@link MongoUnitCollection}s based on the data pointed to by the 'value' or
   * 'locations' (or standard location).
   * @throws MongoUnitException If 'value' or 'locations' point to a file that does not exist or
   * neither 'value' nor 'locations' specify any locations at all and standard locations were
   * likewise unsuccessful (see JavaDoc of {@link SeedWithDataset}).
   */
  private static List<MongoUnitCollection> processSeedWithDatasetAnnotation(
      SeedWithDataset annotation,
      ExtensionContext context,
      String testClassName,
      boolean classLevel) throws MongoUnitException {

    String[] value = annotation.value();
    String[] locations = annotation.locations();
    LocationType locationType = annotation.locationType();
    Class<?> relativePackageClass = context.getRequiredTestClass();

    String[] fileLocations =
        getFileLocations(context, value, locations, classLevel, testClassName, "-seed.json");

    return retrieveDatasetFromLocations(
        fileLocations,
        locationType,
        relativePackageClass,
        testClassName);
  }

  /**
   * @param context Test execution context within which the test is being executed.
   * @param value Value of the 'value' part of xxxDataset annotation.
   * @param locations Value of the 'locations' part of the xxxDataset annotation.
   * @param classLevel True if extracted values were at the class level, false otherwise.
   * @param testClassName Name of the test class, which is either {@link MongoUnitTest} specified
   * name or, if not specified, the simple class name of the test class.
   * @param fileEndingAndExtension String that contains some ending with an extension. (Usually
   * '-seed.json' or '-expected.json' for seeding and assertions accordingly.
   * @return Array of locations. Check if 'value' or 'locations' is a non-empty array. If both are
   * empty, uses 'testClassName' and 'standardExtension' to generate a default file name location
   * based on whether or not this data was from a class level annotation or method level one (which
   * is determined by the provided 'classLevel').
   */
  public static String[] getFileLocations(
      ExtensionContext context,
      String[] value,
      String[] locations,
      boolean classLevel,
      String testClassName,
      String fileEndingAndExtension) {

    String[] fileLocations;

    // Choose locations between 'value', 'locations', or standard locations
    if (value.length != 0) {

      fileLocations = value;

    } else if (locations.length != 0) {

      fileLocations = locations;

    } else {

      // Choose between a class and method based default file name
      String fileName = classLevel ?
          testClassName + fileEndingAndExtension :
          context.getRequiredTestMethod().getName() + fileEndingAndExtension;

      fileLocations = new String[1];
      fileLocations[0] = fileName;
    }

    return fileLocations;
  }

  /**
   * Returns List of {@link MongoUnitCollection}s based on the data pointed to by provided
   * 'fileLocations'.
   *
   * NOTE: returns datasets that may repeat the same collection.
   *
   * @param fileLocations Array paths to the files containing datasets.
   * @param locationType Type of location the provided 'fileLocations' are.
   * @param relativePackageClass If 'locationType' is 'PACKAGE_PLUS_CLASS', this is the class type
   * whose package should be used for package relative 'location' path. Otherwise, it's ignored and
   * can be null.
   * @param testClassName Name of the test class, which is either {@link MongoUnitTest} specified
   * name or, if not specified, the simple class name of the test class.
   * @return List of {@link MongoUnitCollection}s based on the data pointed to by provided
   * 'fileLocations'.
   * @throws MongoUnitException If 'value' or 'locations' point to a file that does not exist or
   * neither 'value' nor 'locations' specify any locations at all and standard locations were
   * likewise unsuccessful.
   */
  public static List<MongoUnitCollection> retrieveDatasetFromLocations(
      String[] fileLocations,
      LocationType locationType,
      Class<?> relativePackageClass,
      String testClassName) throws MongoUnitException {

    // Loop over locations, retrieve dataset content and convert/collect to MongoUnitCollection
    List<MongoUnitCollection> finalMongoUnitCollectionDataset = new ArrayList<>();
    for (String fileLocation : fileLocations) {

      String dataset =
          retrieveResourceFromFile(fileLocation, locationType, relativePackageClass, testClassName);
      List<MongoUnitCollection> mongoUnitCollections = toMongoUnitTypedCollectionsFromJson(dataset);

      finalMongoUnitCollectionDataset.addAll(mongoUnitCollections);
    }
    return finalMongoUnitCollectionDataset;
  }

  /**
   * Returns List of {@link MongoUnitCollection}s based on the data pointed to by the 'value' or
   * 'locations' (or standard location).
   *
   * NOTE: The return does not combine documents from same-named collections into a single list of
   * documents under the same collection.
   *
   * @param annotation Instance of the {@link AssertMatchesDataset} annotation.
   * @param context Test execution context within which the test is being executed.
   * @param testClassName Name of the test class, which is either {@link MongoUnitTest} specified
   * name or, if not specified, the simple class name of the test class.
   * @param classLevel Flag which if set to 'true', indicates that this annotation was placed on a
   * class as opposed to method.
   * @return List of {@link MongoUnitCollection}s based on the data pointed to by the 'value' or
   * 'locations' (or standard location).
   * @throws MongoUnitException If 'value' or 'locations' point to a file that does not exist or
   * neither 'value' nor 'locations' specify any locations at all and standard locations were
   * likewise unsuccessful (see JavaDoc of {@link AssertMatchesDataset}) or the annotation contains
   * mutually exclusive properties ('value'/'locations' is not empty but 'additionalDataset' is set
   * to 'false'.
   */
  private static List<MongoUnitCollection> processAssertMatchesDatasetAnnotation(
      AssertMatchesDataset annotation,
      ExtensionContext context,
      String testClassName,
      boolean classLevel) throws MongoUnitException {

    String[] locations = annotation.locations();
    String[] value = annotation.value();
    LocationType locationType = annotation.locationType();
    boolean additionalDataset = annotation.additionalDataset();
    Class<?> relativePackageClass = context.getRequiredTestClass();

    // Check that mutually exclusive properties are not set
    if (!additionalDataset && (value.length != 0 || locations.length != 0)) {
      String message = "Error: annotation '" + annotation + "' contains mutually exclusive values"
          + " set. If 'additionalDataset' is set to 'false', the annotation is not allowed to have"
          + " neither 'value' nor 'locations' set.";
      log.error(message);
      throw new MongoUnitException(message);
    }

    // Return empty list if 'additionalDataset' is false
    if (!additionalDataset) {
      return new ArrayList<>();
    }

    List<MongoUnitCollection> finalMongoUnitCollectionDataset = new ArrayList<>();

    String[] fileLocations =
        getFileLocations(context, value, locations, classLevel, testClassName, "-expected.json");

    // Loop over locations, retrieve dataset content and convert/collect to MongoUnitCollection
    for (String fileLocation : fileLocations) {
      String dataset =
          retrieveResourceFromFile(fileLocation, locationType, relativePackageClass, testClassName);
      List<MongoUnitCollection> mongoUnitCollections = toMongoUnitTypedCollectionsFromJson(dataset);

      finalMongoUnitCollectionDataset.addAll(mongoUnitCollections);
    }

    return finalMongoUnitCollectionDataset;
  }
}





































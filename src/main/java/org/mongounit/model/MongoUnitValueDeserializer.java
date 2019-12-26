package org.mongounit.model;

import static org.mongounit.MongoUnitUtil.DATE_STRING_FORMAT;
import static org.mongounit.MongoUnitUtil.STANDARD_MONGO_DATE_FORMAT;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.bson.BsonType;
import org.mongounit.MongoUnitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MongoUnitValueDeserializer} class is a custom deserializer for MongoUnit Framework JSON
 * forgot of seed and expected JSON.
 */
public class MongoUnitValueDeserializer extends StdDeserializer<MongoUnitValue> {

  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(MongoUnitValueDeserializer.class);

  /**
   * Indicator that appears as part of a property of a JSON object which indicates that this value
   * is not a document but a MongoUnit Framework specification of a single value.
   */
  private String mongoUnitValueFieldIndicator;

  /**
   * JSON object mapper.
   */
  private ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Default constructor.
   */
  public MongoUnitValueDeserializer() {
    super(MongoUnitValue.class);
  }

  /**
   * Constructor.
   *
   * @param mongoUnitValueFieldIndicator Indicator that appears as part of a property of a JSON
   * object which indicates that this value is not a document but a MongoUnit Framework
   * specification of a single value.
   */
  public MongoUnitValueDeserializer(String mongoUnitValueFieldIndicator) {
    this();
    this.mongoUnitValueFieldIndicator = mongoUnitValueFieldIndicator;
  }

  @Override
  public MongoUnitValue deserialize(JsonParser parser, DeserializationContext context)
      throws IOException, JsonProcessingException {

    // Retrieve JSON node that triggered this deserialization
    JsonNode nodeToDeserialize = parser.getCodec().readTree(parser);
    String propertyName = parser.currentName(); // TODO: not sure I need this
    System.out.println("field name: " + propertyName);

    return toMongoUnitValue(nodeToDeserialize);
  }

  /**
   * @param nodeToDeserialize {@link JsonNode} to deserialize.
   * @return Instance of {@link MongoUnitValue} that represents the JSON.
   * @throws IOException If anything goes wrong parsing the passed in JSON.
   */
  private MongoUnitValue toMongoUnitValue(JsonNode nodeToDeserialize) {

    //    return MongoUnitValue.builder()
    //        .bsonType(BsonType.STRING)
    //        .value("HELLO")
    //        .build();

    Object value;

    switch (nodeToDeserialize.getNodeType()) {

      //      ARRAY,
      //          BINARY,
      //          BOOLEAN,
      //          MISSING,
      //          NULL,
      //          NUMBER,
      //          OBJECT,
      //          POJO,
      //          STRING

      case ARRAY:

        value = toMongoUnitFromArrayValues(nodeToDeserialize);
        //        System.out.println("MongoUnitValue array: " + value);
        break;

      case OBJECT:

        // Is this object actually just a single MongoUnitValue specification
        String mongoUnitValueSpecificationFieldName =
            getMongoUnitValueSpecificationFieldName(nodeToDeserialize);
        if (mongoUnitValueSpecificationFieldName != null) {
          return toSingleMongoUnitValue(nodeToDeserialize, mongoUnitValueSpecificationFieldName);
        }

        value = toMongoUnitValueFromObject(nodeToDeserialize);
        break;
    }

    //    System.out.print(" " + parser.currentName() + " (" + nodeToDeserialize.getNodeType() + ") : ");
    //    System.out.println(nodeToDeserialize);

    return null;
  }

  /**
   * @param mongoUnitValueNode {@link JsonNode} which has the {@link MongoUnitValue} specification
   * fields.
   * @param mongoUnitValueSpecificationFieldName Full field name of a specialized {@link
   * MongoUnitValue} field name with the indicator.
   * @return Instance of a single {@link MongoUnitValue} that is represented by the
   * 'mongoUnitValueNode'.
   */
  private MongoUnitValue toSingleMongoUnitValue(
      JsonNode mongoUnitValueNode,
      String mongoUnitValueSpecificationFieldName) throws MongoUnitException {

    // Attempt to retrieve 'comparator'
    String comparator = null;
    JsonNode comparatorNode = mongoUnitValueNode.get("comparator");
    if (comparatorNode != null) {

      // Make sure it's a string representation; if not throw exception
      if (comparatorNode.getNodeType() != JsonNodeType.STRING) {
        String message = "The 'comparator' field must be a string, but was '"
            + comparatorNode.getNodeType() + "'.";
        log.error(message);
        throw new MongoUnitException(message);
      }

      comparator = comparatorNode.asText();
    }

    // Get JsonNode of the value part of the mongoUnit specification
    JsonNode valueNode = mongoUnitValueNode.get(mongoUnitValueSpecificationFieldName);

    // Get string-based BSON type from the specification
    String stringBsonType =
        mongoUnitValueSpecificationFieldName.substring(mongoUnitValueFieldIndicator.length());

    // If BSON type string is an empty string, no BSON type is specified; keep value raw
    ObjectNode objectNode = (ObjectNode) valueNode; // TODO: how to get regular object there???
    if (stringBsonType.length() == 0) {
      try {
        return MongoUnitValue.builder()
            .bsonType(null)
            .comparator(comparator)
            .value(objectMapper.readValue(valueNode.asText(), Object.class))
            .build();
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }

    // Map string-based BSON type to BsonType enum
    switch (stringBsonType) {

      case "ARRAY":
        return MongoUnitValue.builder()
            .bsonType(BsonType.ARRAY)
            .comparator(comparator)
            .value(toMongoUnitFromArrayValues(valueNode))
            .build();

      case "DOCUMENT":
        return MongoUnitValue.builder()
            .bsonType(BsonType.DOCUMENT)
            .comparator(comparator)
            .value(toMongoUnitValueFromObject(valueNode))
            .build();

      case "DOUBLE":
        return MongoUnitValue.builder()
            .bsonType(BsonType.DOUBLE)
            .comparator(comparator)
            .value(valueNode.asDouble())
            .build();

      case "STRING":
        return MongoUnitValue.builder()
            .bsonType(BsonType.STRING)
            .comparator(comparator)
            .build();

      case "BINARY":
        return MongoUnitValue.builder()
            .bsonType(BsonType.BINARY)
            .comparator(comparator)
            .value(valueNode.asText())
            .build();

      case "OBJECT_ID":
        return MongoUnitValue.builder()
            .bsonType(BsonType.OBJECT_ID)
            .comparator(comparator)
            .value(valueNode.asText())
            .build();

      case "BOOLEAN":
        return MongoUnitValue.builder()
            .bsonType(BsonType.BOOLEAN)
            .comparator(comparator)
            .value(valueNode.asBoolean())
            .build();

      case "DATE_TIME":

        try {

          return MongoUnitValue.builder()
              .bsonType(BsonType.DATE_TIME)
              .comparator(comparator)
              .value(STANDARD_MONGO_DATE_FORMAT.parse(valueNode.asText()).getTime())
              .build();

        } catch (ParseException e) {

          String message = "Date value was not in the supported format of"
              + DATE_STRING_FORMAT + ". Tried to parse '" + valueNode.asText() + "'.";
          log.error(message);
          throw new MongoUnitException(message);
        }

        //      case NULL:
        //      case UNDEFINED:
        //
        //        value = null;
        //        break;
        //
        //      case REGULAR_EXPRESSION:
        //
        //        value = bsonValue.asRegularExpression().getPattern();
        //        break;
        //
        //      case DB_POINTER:
        //        String namespace = bsonValue.asDBPointer().getNamespace();
        //        String objectId = bsonValue.asObjectId().getValue().toHexString();
        //
        //        Map<String, String> dbPointerValueMap = new HashMap<>();
        //        dbPointerValueMap.put("namespace", namespace);
        //        dbPointerValueMap.put("objectId", objectId);
        //
        //        value = dbPointerValueMap;
        //        break;
        //
        //      case JAVASCRIPT:
        //
        //        value = bsonValue.asJavaScript().getCode();
        //        break;
        //
        //      case SYMBOL:
        //
        //        value = bsonValue.asSymbol().getSymbol();
        //        break;
        //
        //      case JAVASCRIPT_WITH_SCOPE:
        //
        //        value = bsonValue.asJavaScriptWithScope().getCode();
        //        break;
        //
        //      case INT32:
        //
        //        value = bsonValue.asInt32().getValue();
        //        break;
        //
        //      case TIMESTAMP:
        //
        //        value = bsonValue.asTimestamp().getValue();
        //        break;
        //
        //      case INT64:
        //
        //        value = bsonValue.asInt64().getValue();
        //        break;
        //
        //      case DECIMAL128:
        //
        //        value = bsonValue.asDecimal128().decimal128Value().bigDecimalValue();
        //        break;
        //
        //      // END_OF_DOCUMENT, MIN_KEY, MAX_KEY
        //      default:
        //        String message = "BSON type " + bsonType + " is not currently supported by the MongoUnit"
        //            + " framework.";
        //        log.error(message);
        //        throw new MongoUnitException(message);
        //    }

    }

    //  /**
    //   * @param mongoUnitValueSpecificationField Full field name of a specialized {@link MongoUnitValue}
    //   * * field name with the indicator.
    //   * @return The {@link BsonType} enum that is encoded into the mongoUnitValue specification field
    //   * or 'null' if none are specified.
    //   */
    //  private BsonType extractBsonType(String mongoUnitValueSpecificationField) {
    //
    //    String stringBsonType =
    //        mongoUnitValueSpecificationField.substring(mongoUnitValueFieldIndicator.length());
    //
    //    // If BSON type string is an empty string, no BSON type is specified
    //    if (stringBsonType.length() == 0) {
    //      return null;
    //    }
    //
    //
    //
    //
    //    }
    //
    return null;
  }

  /**
   * @param objectNode {@link JsonNode} to interpret if it's a {@link MongoUnitValue} specification
   * or a regular object.
   * @return Full field name of a specialized {@link MongoUnitValue} field name with the indicator
   * that it is {@link MongoUnitValue} specification or 'null' if 'objectNode' is just a regular
   * object node and not a {@link MongoUnitValue} specification.
   */
  private String getMongoUnitValueSpecificationFieldName(JsonNode objectNode) {

    // Loop through fields and look for special MongoUnitValue field indicator
    Iterator<String> fieldNameIterator = objectNode.fieldNames();
    while (fieldNameIterator.hasNext()) {

      String fieldName = fieldNameIterator.next();

      if (fieldName.startsWith(mongoUnitValueFieldIndicator)) {
        return fieldName;
      }
    }

    // Went through all field names and didn't find field name with indicator
    return null;
  }

  private Object toMongoUnitValueFromObject(JsonNode objectNode) {

    System.out.println("Object node: " + objectNode);

    Iterator<Entry<String, JsonNode>> fields = objectNode.fields();
    while (fields.hasNext()) {
      Entry<String, JsonNode> field = fields.next();

      System.out.println("Field: " + field);
    }
    System.out.println();

    return null;
  }

  /**
   * @param arrayNode JSON array node that holds an array og {@link JsonNode}s.
   * @return List of {@link MongoUnitValue}s that represent each item in the provided 'arrayNode'
   * array.
   * @throws IOException If anything goes wrong with converting each array item into {@link
   * MongoUnitValue}.
   */
  private List<MongoUnitValue> toMongoUnitFromArrayValues(JsonNode arrayNode) {

    // Loop over JSON node array, convert to MongoUnitValue and add to list of mongoUnit values
    List<MongoUnitValue> mongoUnitArrayValue = new ArrayList<>();
    for (int i = 0; i < arrayNode.size(); i++) {

      mongoUnitArrayValue.add(toMongoUnitValue(arrayNode.get(i)));
    }

    return mongoUnitArrayValue;
  }
}

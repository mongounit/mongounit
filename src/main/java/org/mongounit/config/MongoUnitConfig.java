/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit.config;

/**
 * {@link MongoUnitConfig} class is a holder for mongounit.properties property values.
 */
public class MongoUnitConfig {

  /**
   * Classpath-based mongounit.properties URI.
   */
  public static final String MONGO_UNIT_PROPERTIES_FILE_URI = "/mongounit.properties";

  /**
   * Base URI property name that is looked for in mongounit.properties file.
   */
  public static final String BASE_URI_PROP_NAME = "mongounit.base-uri";

  /**
   * 'Base URI keep as is' property name that is looked for in mongounit.properties file.
   */
  public static final String BASE_URI_KEEP_AS_IS_PROP_NAME = "mongounit.base-uri.keep-as-is";

  /**
   * Name of the property that specifies the field name to indicate that a document represents a
   * special MongoUnit field instead of a regular document.
   */
  public static final String MONGO_UNIT_FIELD_NAME_PROP_NAME = "mongounit.indicator-field-name";

  /**
   * Name of the property that specifies if the test database should be dropped so there is no need
   * for manual cleanup.
   */
  public static final String DROP_DATABASE_PROP_NAME = "mongounit.drop-database";

  /**
   * Name of the property that specifies the time zone ID to use for the database name pad.
   */
  public static final String TIME_ZONE_ID_PROP_NAME = "mongounit.local-time-zone-id";

  /**
   * Default baseUri if nothing else is provided.
   */
  private static final String DEFAULT_BASE_URI = "mongodb://localhost:27017/mongounit-testdb";

  /**
   * By default, the URI should change based on the running user and a random hash.
   */
  private static final boolean DEFAULT_BASE_KEEP_URI_AS_IS = false;

  /**
   * Default field name to use in developer JSON files to signify that a document is a
   * representation of a special MongoUnit value.
   *
   * If the JSON is meant to seed the database, the special field will be in the format of
   * "indicatorBsonType": value, e.g., "$$DATE_TIME": value. The value of the 'BsonType' portion of
   * the field name is to specify a BSON type value based on the naming in {@link
   * org.bson.BsonType}.
   *
   * If the JSON is meant as an expected assertion match, an optional 'comparator' field is expected
   * as a sibling field name.
   */
  public static final String DEFAULT_MONGO_UNIT_VALUE_FIELD_NAME_INDICATOR = "$$";

  /**
   * By default, the test database should be dropped so there is no need for manual cleanup.
   */
  private static final boolean DEFAULT_DROP_DATABASE = true;

  /**
   * Default time zone ID to use for the database name pad.
   */
  private static final String DEFAULT_TIME_ZONE_ID = "UTC";

  /**
   * Base URI to base the dynamic database name on.
   */
  private String baseUri;

  /**
   * Flag to indicate that the base URI should be used for testing as is, without appending anything
   * to it.
   */
  private boolean baseUriKeepAsIs;

  /**
   * Field name to use as an indicator in developer JSON files to signify that a document is a
   * representation of a special MongoUnit value.
   *
   * If the JSON is meant to seed the database, 'bsonType' and 'value' field names are expected as
   * sibling field names. The value of the 'bsonType' field is to specify a BSON type value based on
   * the naming in {@link org.bson.BsonType}.
   *
   * If the JSON is meant as an expected assertion match, an optional 'comparator' field is expected
   * as a sibling field name.
   */
  private String mongoUnitValueFieldNameIndicator;

  /**
   * Flag to indicate if the test database should be dropped after all the tests have run.
   */
  private boolean dropDatabase;

  /**
   * Time zone ID to use in the database name pad.
   */
  private String timeZoneId;

  /**
   * Default constructor.
   */
  public MongoUnitConfig() {
    this.baseUri = DEFAULT_BASE_URI;
    this.baseUriKeepAsIs = DEFAULT_BASE_KEEP_URI_AS_IS;
    this.mongoUnitValueFieldNameIndicator = DEFAULT_MONGO_UNIT_VALUE_FIELD_NAME_INDICATOR;
    this.dropDatabase = DEFAULT_DROP_DATABASE;
    this.timeZoneId = DEFAULT_TIME_ZONE_ID;
  }

  /**
   * Constructor.
   *
   * NOTE: Passing 'null' to any of the arguments sets those arguments to their default value.
   *
   * @param baseUri Base URI to base the dynamic database name on.
   * @param baseUriKeepAsIs Flag to indicate that the base URI should be used for testing as is,
   * without appending anything to it.
   * @param mongoUnitValueFieldNameIndicator Field name to use as an indicator in developer JSON
   * files to signify that a document is a representation of a special MongoUnit BSON value that
   * specifies BSON value type based on the naming in {@link org.bson.BsonType} enum.
   * @param dropDatabase Flag to indicate if the test database should be dropped after all the tests
   * have run.
   * @param timeZoneId Time zone ID to use in the database name pad.
   */
  public MongoUnitConfig(
      String baseUri,
      Boolean baseUriKeepAsIs,
      String mongoUnitValueFieldNameIndicator,
      Boolean dropDatabase,
      String timeZoneId) {

    this.baseUri = baseUri == null ? DEFAULT_BASE_URI : baseUri;
    this.baseUriKeepAsIs = baseUriKeepAsIs == null ? DEFAULT_BASE_KEEP_URI_AS_IS : baseUriKeepAsIs;
    this.mongoUnitValueFieldNameIndicator =
        mongoUnitValueFieldNameIndicator == null ?
            DEFAULT_MONGO_UNIT_VALUE_FIELD_NAME_INDICATOR :
            mongoUnitValueFieldNameIndicator;
    this.dropDatabase = dropDatabase == null ? DEFAULT_DROP_DATABASE : dropDatabase;
    this.timeZoneId = timeZoneId == null ? DEFAULT_TIME_ZONE_ID : timeZoneId;
  }

  /**
   * @return Base URI to base the dynamic database name on.
   */
  public String getBaseUri() {
    return baseUri;
  }

  /**
   * @return Flag to indicate that the base URI should be used for testing as is, without appending
   * anything to it.
   */
  public boolean isBaseUriKeepAsIs() {
    return baseUriKeepAsIs;
  }

  /**
   * Returns ield name to use as an indicator in developer JSON files to signify that a document is
   * a representation of a special MongoUnit value.
   *
   * If the JSON is meant to seed the database, 'bsonType' and 'value' field names are expected as
   * sibling field names. The value of the 'bsonType' field is to specify a BSON type value based on
   * the naming in {@link org.bson.BsonType}.
   *
   * If the JSON is meant as an expected assertion match, 'comparator' field is expected as a
   * singling fiend name.
   *
   * @return Field name to use as an indicator in developer JSON files to signify that a document is
   * a representation of a special MongoUnit value.
   */
  public String getMongoUnitValueFieldNameIndicator() {
    return mongoUnitValueFieldNameIndicator;
  }

  /**
   * @return Flag to indicate if the test database should be dropped after all the tests have run.
   */
  public boolean isDropDatabase() {
    return dropDatabase;
  }

  /**
   * @return Time zone ID to use in the database name pad.
   */
  public String getTimeZoneId() {
    return timeZoneId;
  }

  @Override
  public String toString() {
    return "MongoUnitProperties{" +
        "baseUri='" + baseUri + '\'' +
        ", baseUriKeepAsIs=" + baseUriKeepAsIs +
        ", mongoUnitValueFieldNameIndicator='" + mongoUnitValueFieldNameIndicator + '\'' +
        ", dropDatabase=" + dropDatabase +
        ", timeZoneId='" + timeZoneId + '\'' +
        '}';
  }
}

/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit.config;

import static java.time.ZoneId.SHORT_IDS;
import static org.mongounit.config.MongoUnitProperties.BASE_URI_KEEP_AS_IS_PROP_NAME;
import static org.mongounit.config.MongoUnitProperties.BASE_URI_PROP_NAME;
import static org.mongounit.config.MongoUnitProperties.DROP_DATABASE_PROP_NAME;
import static org.mongounit.config.MongoUnitProperties.MONGO_UNIT_FIELD_NAME_PROP_NAME;
import static org.mongounit.config.MongoUnitProperties.MONGO_UNIT_PROPERTIES_FILE_URI;
import static org.mongounit.config.MongoUnitProperties.TIME_ZONE_ID_PROP_NAME;

import com.mongodb.MongoClientURI;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import org.mongounit.MongoUnitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * {@link MongoUnitConfigurationUtil} class provides utility methods to make accessing
 * 'mongounit.properties' file and its property values easier.
 */
public class MongoUnitConfigurationUtil {

  /**
   * Logger for this configuration class.
   */
  private static Logger log = LoggerFactory.getLogger(MongoUnitConfigurationUtil.class);

  /**
   * Cached instance of {@link MongoUnitProperties}.
   */
  private static MongoUnitProperties cachedMongoUnitProperties;

  /**
   * Returns new {@link MongoClientURI} instance that contains newly generated URI, based on
   * 'mongounit.properties'.
   *
   * @param environment Spring environment to look up existing properties already set, if needed.
   * @return New {@link MongoClientURI} instance that contains newly generated URI, based on
   * mongounit.properties.
   */
  public static MongoClientURI generateNewMongoClientURI(Environment environment) {

    // Retrieve mongounit properties
    MongoUnitProperties mongoUnitProperties = loadMongoUnitProperties();

    boolean keepAsIs = mongoUnitProperties.isBaseUriKeepAsIs();
    String baseUri = mongoUnitProperties.getBaseUri();
    String timeZoneId = mongoUnitProperties.getTimeZoneId();

    // Use default MongoUnit URI
    return generateNewMongoClientURI(baseUri, keepAsIs, timeZoneId);
  }

  /**
   * @param baseUri URI that works as is, but its database name is potentially added to, depending
   * on the 'baseUriKeepAsIs' argument. Can not be 'null'.
   * @param baseUriKeepAsIs Flag to indicate if additional things should be appended to the database
   * name, i.e., the username of the user executing the program and a one-time hash.
   * @param timeZoneId Time zone ID to use in the database name pad.
   * @return {@link MongoClientURI} which is based on the provided 'baseUri'. If 'baseUriKeepAsIs'
   * is false, the username of the user executing the program and a one-time hash is appended to the
   * end of the database name.
   * @throws MongoUnitException If the provided 'baseUri' is not properly formatted such that its
   * database name is not properly configured or the provided 'timeZoneId' can't be parsed.
   */
  public static MongoClientURI generateNewMongoClientURI(
      String baseUri,
      boolean baseUriKeepAsIs,
      String timeZoneId) throws MongoUnitException {

    // Create mongo client uri based on the  baseUri
    MongoClientURI baseMongoClientUri = new MongoClientURI(baseUri);

    // If keeping it as is, return the new mongo client URI
    if (baseUriKeepAsIs) {
      return baseMongoClientUri;
    }

    // Extract database name from original
    String baseUriDbName = baseMongoClientUri.getDatabase();

    // Check it's not null
    if (baseUriDbName == null) {
      String message = "Database name is not properly configured in the URI.";
      log.error(message);
      throw new MongoUnitException(message);
    }

    // Create valid time zone based on provided 'timeZoneId'
    ZoneId timeZone;
    try {
      timeZone = ZoneId.of(timeZoneId, SHORT_IDS);
    } catch (Exception exception) {
      throw new MongoUnitException(
          "Configured Time Zone ID of '" + timeZoneId + "' is not valid.",
          exception);
    }

    // Create date/time portion of db name pad
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
    String dateTimePad =
        new Date().toInstant().atZone(timeZone).toLocalDateTime().format(formatter);

    // Create UUID portion of the db name pad
    String uuidHash = UUID.randomUUID().hashCode() + "";

    // Append username and one-time uuid to base DB name
    String newDbName = baseUriDbName + "_" + System.getProperty("user.name") + "_" + dateTimePad
        + "_" + uuidHash;

    // If the new db name is too long, throw exception (per mongodb docs > 64)
    if (newDbName.length() > 64) {
      String message = "MongoUnit generated a unique database name, based on the one configured in"
          + " 'mongounit.properties' which is too long (>64 chars). Please shorten the database"
          + " name.";
      log.error(message);
      throw new MongoUnitException(message);
    }

    // Create client URI to extract db name only
    String newClientUri = baseUri.replaceFirst(baseUriDbName, newDbName);

    log.info("Using test database with URI: '{}'.", newClientUri);

    return new MongoClientURI(newClientUri);
  }

  /**
   * @return Loaded {@link MongoUnitProperties}.
   */
  @SuppressWarnings("DuplicatedCode") // IntelliJ warning makes no sense
  public static MongoUnitProperties loadMongoUnitProperties() {

    // If cached already, just return the cached version
    if (cachedMongoUnitProperties != null) {
      return cachedMongoUnitProperties;
    }

    Properties mongoUnitProps = new Properties();
    try (
        InputStream in =
            MongoUnitConfigurationUtil.class.getResourceAsStream(MONGO_UNIT_PROPERTIES_FILE_URI)) {

      // Load properties into regular Properties object.
      mongoUnitProps.load(in);
    } catch (Exception e) {
      // Do nothing since we'll fall back on defaults
    }

    // Extract mongounit properties (override with system property if specified)
    String baseUri = mongoUnitProps.getProperty(BASE_URI_PROP_NAME);
    baseUri = useSystemPropertyIfSpecified(BASE_URI_PROP_NAME, baseUri);

    String baseUriKeepAsIsString = mongoUnitProps.getProperty(BASE_URI_KEEP_AS_IS_PROP_NAME);
    baseUriKeepAsIsString =
        useSystemPropertyIfSpecified(BASE_URI_KEEP_AS_IS_PROP_NAME, baseUriKeepAsIsString);
    Boolean baseUriKeepAsIs = null;
    if (baseUriKeepAsIsString != null && !baseUriKeepAsIsString.trim().equals("")) {
      baseUriKeepAsIs = Boolean.parseBoolean(baseUriKeepAsIsString);
    }

    String mongoUnitFieldNameIndicator =
        mongoUnitProps.getProperty(MONGO_UNIT_FIELD_NAME_PROP_NAME);
    mongoUnitFieldNameIndicator =
        useSystemPropertyIfSpecified(MONGO_UNIT_FIELD_NAME_PROP_NAME, mongoUnitFieldNameIndicator);

    String dropDatabaseString = mongoUnitProps.getProperty(DROP_DATABASE_PROP_NAME);
    dropDatabaseString = useSystemPropertyIfSpecified(DROP_DATABASE_PROP_NAME, dropDatabaseString);
    Boolean dropDatabase = null;
    if (dropDatabaseString != null && !dropDatabaseString.trim().equals("")) {
      dropDatabase = Boolean.parseBoolean(dropDatabaseString);
    }

    String timeZoneId = mongoUnitProps.getProperty(TIME_ZONE_ID_PROP_NAME);
    timeZoneId = useSystemPropertyIfSpecified(TIME_ZONE_ID_PROP_NAME, timeZoneId);

    // Build MongoUnitProperties and cache it
    MongoUnitProperties mongoUnitProperties = new MongoUnitProperties(
        baseUri,
        baseUriKeepAsIs,
        mongoUnitFieldNameIndicator,
        dropDatabase,
        timeZoneId);
    cachedMongoUnitProperties = mongoUnitProperties;

    return mongoUnitProperties;
  }

  /**
   * @param key Key with which to check system (command-line) property
   * @param defaultValue Value to return if a system property with the specified 'key' does not
   * exist.
   * @return Value of a system property specified with the provided 'key' or the provided
   * 'defaultValue' if no such property is specified.
   */
  private static String useSystemPropertyIfSpecified(String key, String defaultValue) {

    String systemPropertyValue = System.getProperty(key);

    // Return value if not null, otherwise return default
    if (systemPropertyValue != null) {
      return systemPropertyValue;
    } else {
      return defaultValue;
    }
  }
}

/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit;

import com.mongodb.MongoClientURI;
import java.util.List;
import java.util.Objects;

/**
 * {@link DatasetGeneratorArguments} class represents all arguments passed into the {@link
 * DatasetGenerator} standalone program.
 */
public class DatasetGeneratorArguments {

  /**
   * Validated URI of the mongo DB to connect to.
   */
  private MongoClientURI mongoClientURI;

  /**
   * Optional list of collection names to generate data based on.
   */
  private List<String> collectionNames;

  /**
   * Absolute path to file where to output JSON.
   */
  private String outputPath;

  /**
   * List of BSON types to generate explicit MongoUnit BSON type specification. The specification is
   * based on the names (not values) of the {@link org.bson.BsonType} enum.
   */
  private List<String> preserveBsonTypes;

  /**
   * Field name to use as an indicator in developer JSON files to signify that a document is a
   * representation of a special MongoUnit value.
   *
   * The general format of the special name/value pair is "$$BSON_TYPE": value
   *
   * If the JSON is meant to seed the database, 'BSON_TYPE' is to specify a BSON type value based on
   * the naming in {@link org.bson.BsonType}.
   *
   * If the JSON is meant to assert the expected result state, the format is allowed to be simply:
   * "$$": value. In addition, a 'comparator' field is expected as a sibling field name.
   */
  private String mongoUnitValueFieldNameIndicator;

  /**
   * Default constructor.
   */
  public DatasetGeneratorArguments() {
  }

  /**
   * Constructor.
   *
   * @param mongoClientURI Validated URI of the mongo DB to connect to.
   * @param collectionNames Optional list of collection names to generate data based on.
   * @param outputPath Absolute path to file where to output JSON.
   * @param preserveBsonTypes List of BSON types to generate explicit MongoUnit BSON type
   * specification. The specification is based on the names (not values) of the {@link
   * org.bson.BsonType} enum.
   * @param mongoUnitValueFieldNameIndicator Field name to use as an indicator in developer JSON
   * files to signify that a document is a representation of a special MongoUnit value. The general
   * format of the special name/value pair is "$$BSON_TYPE": value  If the JSON is meant to seed the
   * database, 'BSON_TYPE' is to specify a BSON type value based on the naming in {@link
   * org.bson.BsonType}. If the JSON is meant to assert the expected result state, the format is
   * allowed to be simply:  "$$": value. In addition, a 'comparator' field is expected as a sibling
   * field name.
   */
  @SuppressWarnings("WeakerAccess")
  public DatasetGeneratorArguments(
      MongoClientURI mongoClientURI,
      List<String> collectionNames,
      String outputPath,
      List<String> preserveBsonTypes,
      String mongoUnitValueFieldNameIndicator) {

    this.mongoClientURI = mongoClientURI;
    this.collectionNames = collectionNames;
    this.outputPath = outputPath;
    this.preserveBsonTypes = preserveBsonTypes;
    this.mongoUnitValueFieldNameIndicator = mongoUnitValueFieldNameIndicator;
  }

  /**
   * @return Instance of the builder pattern version of the {@link DatasetGeneratorArguments} class.
   */
  public static DatasetGeneratorArgumentsBuilder builder() {
    return new DatasetGeneratorArgumentsBuilder();
  }

  /**
   * @return Validated URI of the mongo DB to connect to.
   */
  public MongoClientURI getMongoClientURI() {
    return this.mongoClientURI;
  }

  /**
   * @return Optional list of collection names to generate data based on.
   */
  public List<String> getCollectionNames() {
    return this.collectionNames;
  }

  /**
   * @return Absolute path to file where to output JSON.
   */
  public String getOutputPath() {
    return this.outputPath;
  }

  /**
   * @return List of BSON types to generate explicit MongoUnit BSON type specification. The
   * specification is based on the names (not values) of the {@link org.bson.BsonType} enum.
   */
  public List<String> getPreserveBsonTypes() {
    return this.preserveBsonTypes;
  }

  /**
   * @return Field name to use as an indicator in developer JSON files to signify that a document is
   * a representation of a special MongoUnit value. The general format of the special name/value
   * pair is "$$BSON_TYPE": value  If the JSON is meant to seed the database, 'BSON_TYPE' is to
   * specify a BSON type value based on the naming in {@link org.bson.BsonType}. If the JSON is
   * meant to assert the expected result state, the format is allowed to be simply:  "$$": value. In
   * addition, a 'comparator' field is expected as a sibling field name.
   */
  public String getMongoUnitValueFieldNameIndicator() {
    return mongoUnitValueFieldNameIndicator;
  }

  /**
   * @param mongoClientURI Validated URI of the mongo DB to connect to.
   */
  public void setMongoClientURI(MongoClientURI mongoClientURI) {
    this.mongoClientURI = mongoClientURI;
  }

  /**
   * @param collectionNames Optional list of collection names to generate data based on.
   */
  public void setCollectionNames(List<String> collectionNames) {
    this.collectionNames = collectionNames;
  }

  /**
   * @param outputPath Absolute path to file where to output JSON.
   */
  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }

  /**
   * @param preserveBsonTypes List of BSON types to generate explicit MongoUnit BSON type
   * specification. The specification is based on the names (not values) of the {@link
   * org.bson.BsonType} enum.
   */
  public void setPreserveBsonTypes(List<String> preserveBsonTypes) {
    this.preserveBsonTypes = preserveBsonTypes;
  }

  /**
   * @param mongoUnitValueFieldNameIndicator Field name to use as an indicator in developer JSON
   * files to signify that a document is a representation of a special MongoUnit value. The general
   * format of the special name/value pair is "$$BSON_TYPE": value  If the JSON is meant to seed the
   * database, 'BSON_TYPE' is to specify a BSON type value based on the naming in {@link
   * org.bson.BsonType}. If the JSON is meant to assert the expected result state, the format is
   * allowed to be simply:  "$$": value. In addition, a 'comparator' field is expected as a sibling
   * field name.
   */
  public void setMongoUnitValueFieldNameIndicator(String mongoUnitValueFieldNameIndicator) {
    this.mongoUnitValueFieldNameIndicator = mongoUnitValueFieldNameIndicator;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof DatasetGeneratorArguments)) {
      return false;
    }
    final DatasetGeneratorArguments other = (DatasetGeneratorArguments) o;
    if (!other.canEqual(this)) {
      return false;
    }
    final Object this$mongoClientURI = this.getMongoClientURI();
    final Object other$mongoClientURI = other.getMongoClientURI();
    if (!Objects.equals(this$mongoClientURI, other$mongoClientURI)) {
      return false;
    }
    final Object this$collectionNames = this.getCollectionNames();
    final Object other$collectionNames = other.getCollectionNames();
    if (!Objects.equals(this$collectionNames, other$collectionNames)) {
      return false;
    }
    final Object this$outputPath = this.getOutputPath();
    final Object other$outputPath = other.getOutputPath();
    if (!Objects.equals(this$outputPath, other$outputPath)) {
      return false;
    }
    final Object this$mongoUnitValueFieldNameIndicator = this.getMongoUnitValueFieldNameIndicator();
    final Object other$mongoUnitValueFieldNameIndicator =
        other.getMongoUnitValueFieldNameIndicator();
    if (!Objects.equals(
        this$mongoUnitValueFieldNameIndicator,
        other$mongoUnitValueFieldNameIndicator)) {
      return false;
    }
    final Object this$preserveBsonTypes = this.getPreserveBsonTypes();
    final Object other$preserveBsonTypes = other.getPreserveBsonTypes();
    return Objects.equals(this$preserveBsonTypes, other$preserveBsonTypes);
  }

  /**
   * @param other Object to test possible equality with.
   * @return true if 'other' can equal this object based on 'other''s type, false otherwise.
   */
  private boolean canEqual(final Object other) {
    return other instanceof DatasetGeneratorArguments;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $mongoClientURI = this.getMongoClientURI();
    result = result * PRIME + ($mongoClientURI == null ? 43 : $mongoClientURI.hashCode());
    final Object $collectionNames = this.getCollectionNames();
    result = result * PRIME + ($collectionNames == null ? 43 : $collectionNames.hashCode());
    final Object $outputPath = this.getOutputPath();
    result = result * PRIME + ($outputPath == null ? 43 : $outputPath.hashCode());
    final Object $preserveBsonTypes = this.getPreserveBsonTypes();
    result = result * PRIME + ($preserveBsonTypes == null ? 43 : $preserveBsonTypes.hashCode());
    final Object $mongoUnitValueFieldNameIndicator = this.getMongoUnitValueFieldNameIndicator();
    result = result * PRIME + ($mongoUnitValueFieldNameIndicator == null ? 43
        : $mongoUnitValueFieldNameIndicator.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "DatasetGeneratorArguments(mongoClientURI=" + this.getMongoClientURI()
        + ", collectionNames=" + this.getCollectionNames() + ", outputPath=" + this.getOutputPath()
        + ", preserveBsonTypes=" + this.getPreserveBsonTypes()
        + ", mongoUnitValueFieldNameIndicator=" + this
        .getMongoUnitValueFieldNameIndicator() + ")";
  }

  /**
   * {@link DatasetGeneratorArgumentsBuilder} class is a builder pattern class for the {@link
   * DatasetGeneratorArguments} class.
   */
  public static class DatasetGeneratorArgumentsBuilder {

    /**
     * Validated URI of the mongo DB to connect to.
     */
    private MongoClientURI mongoClientURI;

    /**
     * Optional list of collection names to generate data based on.
     */
    private List<String> collectionNames;

    /**
     * Absolute path to file where to output JSON.
     */
    private String outputPath;

    /**
     * List of BSON types to generate explicit MongoUnit BSON type specification. The specification
     * is based on the names (not values) of the {@link org.bson.BsonType} enum.
     */
    private List<String> preserveBsonTypes;

    /**
     * Field name to use as an indicator in developer JSON files to signify that a document is a
     * representation of a special MongoUnit value.
     *
     * The general format of the special name/value pair is "$$BSON_TYPE": value
     *
     * If the JSON is meant to seed the database, 'BSON_TYPE' is to specify a BSON type value based
     * on the naming in {@link org.bson.BsonType}.
     *
     * If the JSON is meant to assert the expected result state, the format is allowed to be simply:
     * "$$": value. In addition, a 'comparator' field is expected as a sibling field name.
     */
    private String mongoUnitValueFieldNameIndicator;

    /**
     * Constructor.
     */
    DatasetGeneratorArgumentsBuilder() {
    }

    /**
     * @param mongoClientURI Validated URI of the mongo DB to connect to.
     * @return Instance of this builder class.
     */
    public DatasetGeneratorArguments.DatasetGeneratorArgumentsBuilder mongoClientURI(
        MongoClientURI mongoClientURI) {
      this.mongoClientURI = mongoClientURI;
      return this;
    }

    /**
     * @param collectionNames Optional list of collection names to generate data based on.
     * @return Instance of this builder class.
     */
    public DatasetGeneratorArguments.DatasetGeneratorArgumentsBuilder collectionNames(
        List<String> collectionNames) {
      this.collectionNames = collectionNames;
      return this;
    }

    /**
     * @param outputPath Absolute path to file where to output JSON.
     * @return Instance of this builder class.
     */
    public DatasetGeneratorArguments.DatasetGeneratorArgumentsBuilder outputPath(
        String outputPath) {
      this.outputPath = outputPath;
      return this;
    }

    /**
     * @param preserveBsonTypes List of BSON types to generate explicit MongoUnit BSON type
     * specification. The specification is based on the names (not values) of the {@link
     * org.bson.BsonType} enum.
     * @return Instance of this builder class.
     */
    public DatasetGeneratorArguments.DatasetGeneratorArgumentsBuilder preserveBsonTypes(
        List<String> preserveBsonTypes) {
      this.preserveBsonTypes = preserveBsonTypes;
      return this;
    }

    /**
     * @param mongoUnitValueFieldNameIndicator Field name to use as an indicator in developer JSON
     * files to signify that a document is a representation of a special MongoUnit value. The
     * general format of the special name/value pair is "$$BSON_TYPE": value  If the JSON is meant
     * to seed the database, 'BSON_TYPE' is to specify a BSON type value based on the naming in
     * {@link org.bson.BsonType}. If the JSON is meant to assert the expected result state, the
     * format is allowed to be simply:  "$$": value. In addition, a 'comparator' field is expected
     * as a sibling field name.
     * @return Instance of this builder class.
     */
    public DatasetGeneratorArguments.DatasetGeneratorArgumentsBuilder mongoUnitValueFieldNameIndicator(
        String mongoUnitValueFieldNameIndicator) {
      this.mongoUnitValueFieldNameIndicator = mongoUnitValueFieldNameIndicator;
      return this;
    }

    /**
     * @return New instance of the {@link DatasetGeneratorArguments} class with the previously set
     * properties.
     */
    public DatasetGeneratorArguments build() {
      return new DatasetGeneratorArguments(
          mongoClientURI,
          collectionNames,
          outputPath,
          preserveBsonTypes,
          mongoUnitValueFieldNameIndicator);
    }

    @Override
    public String toString() {
      return "DatasetGeneratorArguments.DatasetGeneratorArgumentsBuilder(mongoClientURI="
          + this.mongoClientURI + ", collectionNames=" + this.collectionNames + ", outputPath="
          + this.outputPath + ", preserveBsonTypes=" + this.preserveBsonTypes
          + ", mongoUnitValueFieldNameIndicator=" + this.mongoUnitValueFieldNameIndicator + ")";
    }
  }
}

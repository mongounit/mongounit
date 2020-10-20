/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.mongounit.model.MongoUnitCollection.MongoUnitCollectionBuilder;

/**
 * {@link InternalMongoUnitCollection} class contains information for easier deserialization from
 * the custom JSON of MongoUnit framework.
 */
public class InternalMongoUnitCollection {

  /**
   * Name of the collection.
   */
  private String collectionName;

  /**
   * List of maps of field name/value pairs of all the documents in this collection, where each map
   * represents a single document.
   */
  private List<Map<String, Object>> documents;

  /**
   * Default constructor.
   */
  public InternalMongoUnitCollection() {
  }

  /**
   * Constructor.
   *
   * @param collectionName Name of the collection.
   * @param documents List of maps of field name/value pairs of all the documents in this
   * collection, where each map represents a single document.
   */
  @SuppressWarnings("WeakerAccess")
  public InternalMongoUnitCollection(String collectionName, List<Map<String, Object>> documents) {
    this.collectionName = collectionName;
    this.documents = documents;
  }

  /**
   * @return Instance of the builder pattern version of the {@link MongoUnitCollection} class.
   */
  public static MongoUnitCollectionBuilder builder() {
    return new MongoUnitCollectionBuilder();
  }

  /**
   * @return Name of the collection.
   */
  public String getCollectionName() {
    return this.collectionName;
  }

  /**
   * @return List of maps of field name/value pairs of all the documents in this collection, where
   * each map represents a single document.
   */
  public List<Map<String, Object>> getDocuments() {
    return this.documents;
  }

  /**
   * @param collectionName Name of the collection.
   */
  public void setCollectionName(String collectionName) {
    this.collectionName = collectionName;
  }

  /**
   * @param documents List of maps of field name/value pairs of all the documents in this
   * collection, where each map represents a single document.
   */
  public void setDocuments(List<Map<String, Object>> documents) {
    this.documents = documents;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof InternalMongoUnitCollection)) {
      return false;
    }
    final InternalMongoUnitCollection other = (InternalMongoUnitCollection) o;
    if (!other.canEqual(this)) {
      return false;
    }
    final Object this$collectionName = this.getCollectionName();
    final Object other$collectionName = other.getCollectionName();
    if (!Objects.equals(this$collectionName, other$collectionName)) {
      return false;
    }
    final Object this$documents = this.getDocuments();
    final Object other$documents = other.getDocuments();
    return Objects.equals(this$documents, other$documents);
  }

  /**
   * @param other Object to test possible equality with.
   * @return true if 'other' can equal this object based on 'other''s type, false otherwise.
   */
  private boolean canEqual(final Object other) {
    return other instanceof MongoUnitCollection;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $collectionName = this.getCollectionName();
    result = result * PRIME + ($collectionName == null ? 43 : $collectionName.hashCode());
    final Object $documents = this.getDocuments();
    result = result * PRIME + ($documents == null ? 43 : $documents.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "MongoUnitCollection(collectionName=" + this.getCollectionName() + ", documents=" + this
        .getDocuments() + ")";
  }

  /**
   * {@link InternalMongoUnitCollectionBuilder} class is a builder pattern class for the {@link
   * InternalMongoUnitCollection} class.
   */
  public static class InternalMongoUnitCollectionBuilder {

    /**
     * Name of the collection.
     */
    private String collectionName;

    /**
     * List of maps of field name/value pairs of all the documents in this collection, where each
     * map represents a single document.
     */
    private List<Map<String, Object>> documents;

    /**
     * Default constructor.
     */
    InternalMongoUnitCollectionBuilder() {
    }

    /**
     * @param collectionName Name of the collection.
     * @return Instance of this builder class.
     */
    public InternalMongoUnitCollection.InternalMongoUnitCollectionBuilder collectionName(
        String collectionName) {
      this.collectionName = collectionName;
      return this;
    }

    /**
     * @param documents List of maps of field name/value pairs of all the documents in this
     * collection, where each map represents a single document.
     * @return Instance of this builder class.
     */
    public InternalMongoUnitCollection.InternalMongoUnitCollectionBuilder documents(
        List<Map<String, Object>> documents) {
      this.documents = documents;
      return this;
    }

    /**
     * @return New instance of the {@link InternalMongoUnitCollection} class with the previously set
     * properties.
     */
    public InternalMongoUnitCollection build() {
      return new InternalMongoUnitCollection(collectionName, documents);
    }

    @Override
    public String toString() {
      return "MongoUnitCollection.MongoUnitCollectionBuilder(collectionName=" + this.collectionName
          + ", documents=" + this.documents + ")";
    }
  }
}

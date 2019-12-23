/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit.model;

import java.util.Objects;
import org.bson.BsonType;

/**
 * {@link MongoUnitValue} class represents a value with an optional comparator.
 */
@SuppressWarnings("WeakerAccess")
public class MongoUnitValue {

  /**
   * Optional BsonType of the MongoUnit value.
   */
  private BsonType bsonType;

  /**
   * Actual value.
   */
  private Object value;

  /**
   * Optional comparator to use with the value.
   */
  private String comparator;

  /**
   * Default constructor.
   */
  public MongoUnitValue() {
  }

  /**
   * Constructor.
   *
   * @param bsonType Optional BsonType of the MongoUnit value.
   * @param value Actual value.
   * @param comparator Optional comparator to use with the value.
   */
  public MongoUnitValue(BsonType bsonType, Object value, String comparator) {
    this.bsonType = bsonType;
    this.value = value;
    this.comparator = comparator;
  }

  /**
   * @return Instance of the builder pattern version of the {@link MongoUnitValue} class.
   */
  public static MongoUnitValueBuilder builder() {
    return new MongoUnitValueBuilder();
  }

  /**
   * @return BsonType of the MongoUnit value.
   */
  public BsonType getBsonType() {
    return this.bsonType;
  }

  /**
   * @return Actual value.
   */
  public Object getValue() {
    return this.value;
  }

  /**
   * @return Comparator value of the MongoUnit value.
   */
  public String getComparator() {
    return this.comparator;
  }

  /**
   * @param bsonType BsonType of the MongoUnit value.
   */
  public void setBsonType(BsonType bsonType) {
    this.bsonType = bsonType;
  }

  /**
   * @param value Actual value.
   */
  public void setValue(Object value) {
    this.value = value;
  }

  /**
   * @param comparator Comparator value of the MongoUnit value.
   */
  public void setComparator(String comparator) {
    this.comparator = comparator;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof MongoUnitValue)) {
      return false;
    }
    final MongoUnitValue other = (MongoUnitValue) o;
    if (!other.canEqual(this)) {
      return false;
    }
    final Object this$bsonType = this.getBsonType();
    final Object other$bsonType = other.getBsonType();
    if (!Objects.equals(this$bsonType, other$bsonType)) {
      return false;
    }
    final Object this$value = this.getValue();
    final Object other$value = other.getValue();
    if (!Objects.equals(this$value, other$value)) {
      return false;
    }
    final Object this$comparator = this.getComparator();
    final Object other$comparator = other.getComparator();
    return Objects.equals(this$comparator, other$comparator);
  }

  /**
   * @param other Object to test possible equality with.
   * @return true if 'other' can equal this object based on 'other''s type, false otherwise.
   */
  protected boolean canEqual(final Object other) {
    return other instanceof MongoUnitValue;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $bsonType = this.getBsonType();
    result = result * PRIME + ($bsonType == null ? 43 : $bsonType.hashCode());
    final Object $value = this.getValue();
    result = result * PRIME + ($value == null ? 43 : $value.hashCode());
    final Object $comparatorValue = this.getComparator();
    result = result * PRIME + ($comparatorValue == null ? 43 : $comparatorValue.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "MongoUnitValue(bsonType=" + this.getBsonType() + ", value=" + this.getValue()
        + ", comparator=" + this.getComparator() + ")";
  }

  /**
   * {@link MongoUnitValueBuilder} class is a builder pattern class for the {@link MongoUnitValue}
   * class.
   */
  public static class MongoUnitValueBuilder {

    /**
     * Optional BsonType of the MongoUnit value.
     */
    private BsonType bsonType;

    /**
     * Actual value.
     */
    private Object value;

    /**
     * Optional comparator to use with the value.
     */
    private String comparator;

    /**
     * Default constructor.
     */
    MongoUnitValueBuilder() {
    }

    /**
     * @param bsonType Optional BsonType of the MongoUnit value.
     * @return Instance of this builder class.
     */
    public MongoUnitValue.MongoUnitValueBuilder bsonType(BsonType bsonType) {
      this.bsonType = bsonType;
      return this;
    }

    /**
     * @param value Actual value.
     * @return Instance of this builder class.
     */
    public MongoUnitValue.MongoUnitValueBuilder value(Object value) {
      this.value = value;
      return this;
    }

    /**
     * @param comparator Optional comparator to use with the value.
     * @return Instance of this builder class.
     */
    public MongoUnitValue.MongoUnitValueBuilder comparator(String comparator) {
      this.comparator = comparator;
      return this;
    }

    /**
     * @return New instance of the {@link MongoUnitValue} class with the previously set properties.
     */
    public MongoUnitValue build() {
      return new MongoUnitValue(bsonType, value, comparator);
    }

    @Override
    public String toString() {
      return "MongoUnitValue.MongoUnitValueBuilder(bsonType=" + this.bsonType + ", value="
          + this.value + ", comparator=" + this.comparator + ")";
    }
  }
}

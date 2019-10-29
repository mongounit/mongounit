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

/**
 * {@link MongoUnitValue} class represents the fields and values represented by the special format
 * of the MongoUnit value.
 */
@SuppressWarnings("WeakerAccess")
public class MongoUnitValue {

  /**
   * BsonType of the MongoUnit value.
   */
  private String bsonType;

  /**
   * Actual value.
   */
  private Object value;

  /**
   * Comparator value of the MongoUnit value.
   */
  private String comparatorValue;

  /**
   * Default constructor.
   */
  public MongoUnitValue() {
  }

  /**
   * Constructor.
   *
   * @param bsonType BsonType of the MongoUnit value.
   * @param value Actual value.
   * @param comparatorValue Comparator value of the MongoUnit value.
   */
  public MongoUnitValue(String bsonType, Object value, String comparatorValue) {
    this.bsonType = bsonType;
    this.value = value;
    this.comparatorValue = comparatorValue;
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
  public String getBsonType() {
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
  public String getComparatorValue() {
    return this.comparatorValue;
  }

  /**
   * @param bsonType BsonType of the MongoUnit value.
   */
  public void setBsonType(String bsonType) {
    this.bsonType = bsonType;
  }

  /**
   * @param value Actual value.
   */
  public void setValue(Object value) {
    this.value = value;
  }

  /**
   * @param comparatorValue Comparator value of the MongoUnit value.
   */
  public void setComparatorValue(String comparatorValue) {
    this.comparatorValue = comparatorValue;
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
    final Object this$comparatorValue = this.getComparatorValue();
    final Object other$comparatorValue = other.getComparatorValue();
    return Objects.equals(this$comparatorValue, other$comparatorValue);
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
    final Object $comparatorValue = this.getComparatorValue();
    result = result * PRIME + ($comparatorValue == null ? 43 : $comparatorValue.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "MongoUnitValue(bsonType=" + this.getBsonType() + ", value=" + this.getValue()
        + ", comparatorValue=" + this.getComparatorValue() + ")";
  }

  /**
   * {@link MongoUnitValueBuilder} class is a builder pattern class for the {@link MongoUnitValue}
   * class.
   */
  public static class MongoUnitValueBuilder {

    /**
     * BsonType of the MongoUnit value.
     */
    private String bsonType;

    /**
     * Actual value.
     */
    private Object value;

    /**
     * Comparator value of the MongoUnit value.
     */
    private String comparatorValue;

    /**
     * Default constructor.
     */
    MongoUnitValueBuilder() {
    }

    /**
     * @param bsonType BsonType of the MongoUnit value.
     * @return Instance of this builder class.
     */
    public MongoUnitValue.MongoUnitValueBuilder bsonType(String bsonType) {
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
     * @param comparatorValue Comparator value of the MongoUnit value.
     * @return Instance of this builder class.
     */
    public MongoUnitValue.MongoUnitValueBuilder comparatorValue(String comparatorValue) {
      this.comparatorValue = comparatorValue;
      return this;
    }

    /**
     * @return New instance of the {@link MongoUnitValue} class with the previously set properties.
     */
    public MongoUnitValue build() {
      return new MongoUnitValue(bsonType, value, comparatorValue);
    }

    @Override
    public String toString() {
      return "MongoUnitValue.MongoUnitValueBuilder(bsonType=" + this.bsonType + ", value="
          + this.value + ", comparatorValue=" + this.comparatorValue + ")";
    }
  }
}

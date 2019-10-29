/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/**
 * {@link AssertMatchesDataset} annotation specifies location(s) of a JSON-based file(s) with data
 * to match the state of the database with.
 *
 * It can be used on a class, on a method, or in combination of the two, which would have a
 * cumulative effect, i.e., the data pointed to by the method annotation would be added to the data
 * pointed to by the class annotation.
 *
 * There is also an option to trigger assertion with no additional dataset by setting this
 * annotation's 'additionalDataset' to 'false'. In such a case, previously set assertion datasets
 * (if any at all) are used. This can be used to verify that nothing exists in the database or if
 * the dataset is coming from {@link SeedWithDataset} annotation with the 'reuseForAssertion' set to
 * 'true'.
 *
 * If neither the target test method nor its containing target test class is annotated with this
 * annotation, no automatic assertion takes place.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(AssertMatchesDatasets.class)
public @interface AssertMatchesDataset {

  /**
   * List of locations of JSON files that contain the dataset to match the state the database with.
   *
   * If the 'locationType' is 'CLASSPATH_ROOT', the paths are assumed to be relative to the root of
   * the classpath or in the directory 'mongounit' located at the root of the classpath.
   *
   * Specifying this annotation on a class or a method without providing at least a single location
   * should cause the MongoUnit framework to look for a default file named 'ClassName-expected.json'
   * or 'methodName-expected.json' (depending on the location of the annotation) at the root of the
   * classpath or in the directory 'mongounit' located at the root of the classpath.
   *
   * If the MongoUnit framework fails to find any of the dataset file(s), an exception will be
   * thrown.
   *
   * If the desired assertion state is an empty dataset, set 'additionalDataset = false' and omit
   * setting this property.
   *
   * Multiple locations have a cumulative effect on the data, i.e., the data is sequentially
   * combined in the order specified in the 'locations' array before a match assertion is made.
   *
   * @return List of locations of JSON files that contain the dataset to match the state the
   * database with.
   */
  @AliasFor("locations")
  String[] value() default {};

  /**
   * List of locations of JSON files that contain the dataset to match the state the database with.
   *
   * If the 'locationType' is 'CLASSPATH_ROOT', the paths are assumed to be relative to the root of
   * the classpath or in the directory 'mongounit' located at the root of the classpath.
   *
   * Specifying this annotation on a class or a method without providing at least a single location
   * should cause the MongoUnit framework to look for a default file named 'ClassName-expected.json'
   * or 'methodName-expected.json' (depending on the location of the annotation) at the root of the
   * classpath or in the directory 'mongounit' located at the root of the classpath.
   *
   * If the MongoUnit framework fails to find any of the dataset file(s), an exception will be
   * thrown.
   *
   * If the desired assertion state is an empty dataset, set 'additionalDataset = false' and omit
   * setting this property.
   *
   * Multiple locations have a cumulative effect on the data, i.e., the data is sequentially
   * combined in the order specified in the 'locations' array before a match assertion is made.
   *
   * @return List of locations of JSON files that contain the dataset to match the state the
   * database with.
   */
  @AliasFor("value")
  String[] locations() default {};

  /**
   * @return Dictates how to treat paths specified in 'locations' or its alias 'value'.
   */
  LocationType locationType() default LocationType.CLASSPATH_ROOT;

  /**
   * Returns true to indicate additional assertion dataset should be used for this assertion based
   * on the 'value' or 'locations' pointed to by this annotation, false if no additional dataset
   * should be loaded.
   *
   * @return Flag to indicate if additional assertion dataset should be used for this assertion,
   * i.e., if the datasets (or lack thereof) that have already been defined should be used to assert
   * match without any additional datasets or if additional datasets, pointed to by this annotation,
   * should be loaded. If the value of this property is set to 'false', neither the 'locations' nor
   * 'value' properties can be set to anything other than their default, i.e., empty arrays. In
   * other words, setting 'additionalDataset' to 'false' is mutually exclusive of 'locations' and
   * 'value' properties.
   */
  boolean additionalDataset() default true;
}

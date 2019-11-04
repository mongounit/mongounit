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
 * {@link SeedWithDataset} annotation specifies location(s) of a JSON-based file(s) with data to
 * seed the database before a target test is executed. It can be used on a class, on a method, or in
 * combination of the two which would have a cumulative effect, i.e., the data pointed to by the
 * method annotation would be added to the data pointed to by the class annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(SeedWithDatasets.class)
public @interface SeedWithDataset {

  /**
   * List of locations of JSON files that contain the dataset to seed the database with.
   *
   * If the 'locationType' is 'CLASSPATH_ROOT', the paths are assumed to be relative to the root of
   * the classpath or in the directory 'mongounit' located at the root of the classpath.
   *
   * Specifying this annotation on a class or a method without providing at least a single location
   * should cause the MongoUnit framework to look for a default file named 'ClassName-seed.json' or
   * 'methodName-seed.json' (depending on the location of the annotation) at the root of the
   * classpath or in the directory 'mongounit' located at the root of the classpath.
   *
   * If the MongoUnit framework fails to find any of the the seed data file(s), an exception will be
   * thrown.
   *
   * If the desired initial/seeded state is an empty database, omit this annotation altogether.
   *
   * Multiple locations have a cumulative seeding effect, i.e., the data is seeded into the database
   * sequentially and is combined in the order specified in the 'locations' array before a test
   * method is executed.
   *
   * @return List of locations of JSON files that contain the dataset to seed the database with.
   */
  @AliasFor("locations")
  String[] value() default {};

  /**
   * List of locations of JSON files that contain the dataset to seed the database with.
   *
   * If the 'locationType' is 'CLASSPATH_ROOT', the paths are assumed to be relative to the root of
   * the classpath or in the directory 'mongounit' located at the root of the classpath.
   *
   * Specifying this annotation on a class or a method without providing at least a single location
   * should cause the MongoUnit framework to look for a default file named 'ClassName-seed.json' or
   * 'methodName-seed.json' (depending on the location of the annotation) at the root of the
   * classpath or in the directory 'mongounit' located at the root of the classpath.
   *
   * If the MongoUnit framework fails to find any of the the seed data file(s), an exception will be
   * thrown.
   *
   * If the desired initial/seeded state is an empty database, omit this annotation altogether.
   *
   * Multiple locations have a cumulative seeding effect, i.e., the data is seeded into the database
   * sequentially and is combined in the order specified in the 'locations' array before a test
   * method is executed.
   *
   * @return List of locations of JSON files that contain the dataset to seed the database with.
   */
  @AliasFor("value")
  String[] locations() default {};


  /**
   * @return Dictates how to treat paths specified in 'locations' or its alias 'value'.
   */
  LocationType locationType() default LocationType.PACKAGE_PLUS_CLASS;

  /**
   * Returns whether or not the dataset(s) of this annotation should also be used when building up
   * the dataset(s) with which to assert.
   *
   * @return Whether or not the dataset(s) of this annotation should also be used when building up
   * the dataset(s) with which to assert.
   * <p>
   * For example, if a dataset is not supposed to be altered in any way, this flag can be used so
   * there is no need for a different JSON file or a separate {@link AssertMatchesDataset}
   * annotation containing the exact same data to be loaded again.
   * </p>
   * <p>
   * Another example would be if a dataset is simply used as a supporting dataset to the dataset
   * that's actually not going to change as a result of some target test method call. The supporting
   * dataset should not change and can be reused to assertion.
   * </p>
   * <p>
   * NOTE: If the assertion is simply verifying that nothing changed after the execution of the
   * target test method, such {@link AssertMatchesDataset} annotation should use the
   * 'additionalDataset = false'. See JavaDoc for {@link AssertMatchesDataset}.
   * </p>
   */
  boolean reuseForAssertion() default false;
}

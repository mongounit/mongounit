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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import org.springframework.core.annotation.AliasFor;

/**
 * {@link SnapshotAfterExecution} annotation specifies location(s) of a JSON-based file(s) with data
 * to seed the database before a target test is executed. It can be used on a class, on a method, or
 * in combination of the two which would have a cumulative effect, i.e., the data pointed to by the
 * method annotation would be added to the data pointed to by the class annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SnapshotAfterExecution {

  /**
   * List of locations of JSON files that contain the dataset to seed the database with.
   *
   * Specifying this annotation on a class or a method without providing at least a single location
   * should cause the MongoUnit framework to look for a default file named 'ClassName-seed.json' or
   * 'methodName-seed.json' (depending on the location of the annotation). The directory in which
   * this file will be looked for depends on the provided 'locationType'.
   *
   * If 'locationType' is 'CLASSPATH_ROOT', the file will be looked for at the classpath root.
   *
   * If the 'locationType' is 'CLASS', the file will be looked for at theclasspath root plus the
   * package structure along with class name (or 'name' specified by {@link MongoUnitTest}), i.e.,
   * if the fully qualified name of the test class is 'com.mytest.MyIT' and this value is selected,
   * the file will be searched for in the '/com/mytest/MyIT' folder relative to the root of the
   * classpath. If 'com.mytest.MyIT' class' {@link MongoUnitTest} annotation specifies 'name' of
   * 'test1', the file will be searched for in the '/com/mytest/test1' folder relative to the root
   * of the classpath.
   *
   * If the 'locationType' is 'ABSOLUTE', the file will be looked for in the root directory, i.e .,
   * '/'.
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
  @AliasFor("location")
  String value();

  /**
   * List of locations of JSON files that contain the dataset to seed the database with.
   *
   * Specifying this annotation on a class or a method without providing at least a single location
   * should cause the MongoUnit framework to look for a default file named 'ClassName-seed.json' or
   * 'methodName-seed.json' (depending on the location of the annotation). The directory in which
   * this file will be looked for depends on the provided 'locationType'.
   *
   * If 'locationType' is 'CLASSPATH_ROOT', the file will be looked for at the classpath root.
   *
   * If the 'locationType' is 'CLASS', the file will be looked for at theclasspath root plus the
   * package structure along with class name (or 'name' specified by {@link MongoUnitTest}), i.e.,
   * if the fully qualified name of the test class is 'com.mytest.MyIT' and this value is selected,
   * the file will be searched for in the '/com/mytest/MyIT' folder relative to the root of the
   * classpath. If 'com.mytest.MyIT' class' {@link MongoUnitTest} annotation specifies 'name' of
   * 'test1', the file will be searched for in the '/com/mytest/test1' folder relative to the root
   * of the classpath.
   *
   * If the 'locationType' is 'ABSOLUTE', the file will be looked for in the root directory, i.e .,
   * '/'.
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
  String location();


  /**
   * @return Dictates how to treat paths specified in 'locations' or its alias 'value'.
   */
  LocationType locationType() default LocationType.CLASS;

  String[] collectionNames() default {};

  String[] preserveBsonTypes() default {};
}

/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mongounit.config.MongoUnitConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

/**
 * {@link MongoUnitTest} is an annotation meant to be placed on JUnit classes to autoconfigure the
 * Spring-based MongoDbFactory to use a test database for the integration tests.
 * <p>
 * Placing this annotation on a test class automatically triggers the MongoUnit framework to look
 * for them as system properties or to look for the 'mongounit.properties' file at the root of the
 * classpath. If such a file is not found, the following defaults are used:
 * </p>
 * <p>mongounit.base-uri = mongodb://localhost:27017/mongounit-testdb </p>
 * <p>mongounit.base-uri.keep-as-is = false </p>
 * <p>mongounit.drop-database = true </p>
 * <p>mongounit.indicator-field-name = $$ </p>
 * <p>mongounit.local-time-zone-id = UTC </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(MongoUnitExtension.class)
@ImportAutoConfiguration(classes = MongoUnitConfiguration.class)
public @interface MongoUnitTest {

  /**
   * @return The class name of this test. This property affects what subfolder name and class-level
   * dataset name is automatically looked for when resolving locations for the datasets specified in
   * {@link SeedWithDataset} and {@link AssertMatchesDataset} annotations. The default is an empty
   * string, which triggers MongoUnit framework to use the simple class name this annotation is on
   * as the name for the subfolder and the class-level dataset name.
   */
  String name() default "";
}

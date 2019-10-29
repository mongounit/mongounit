/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.MongoClientURI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

/**
 * {@link MongoUnitConfigurationUtilTest} is a test class for {@link MongoUnitConfigurationUtil}.
 */
@DisplayName("MongoClientURI generation")
class MongoUnitConfigurationUtilTest {

  @Test
  @DisplayName("Through mongounit.properties")
  void testGenerateNewMongoClientURIWithEnvironment() {

    // Mock Environment (won't need it anyway)
    Environment environment = Mockito.mock(Environment.class);

    MongoClientURI actualMongoClientURI = MongoUnitConfigurationUtil
        .generateNewMongoClientURI(environment);
    String actualDbName = actualMongoClientURI.getDatabase();

    assert actualDbName != null;
    assertEquals("mytestdb", actualDbName.substring(0, 8));
    assertTrue(actualDbName.length() > 8);
  }

  @Test
  @DisplayName("Based on baseUri and keep as is flag.")
  void testGenerateNewMongoClientURIFromString() {

    String baseUri = "mongodb://someuser:somepassword@localhost:27017/demodb?connectTimeoutMS=300000";
    MongoClientURI actualUri = MongoUnitConfigurationUtil
        .generateNewMongoClientURI(baseUri, false, "UTC");

    String actualDbName = actualUri.getDatabase();

    assert actualDbName != null;
    assertEquals("demodb", actualDbName.substring(0, 6));
    assertTrue(actualDbName.length() > 6);

    actualUri = MongoUnitConfigurationUtil.generateNewMongoClientURI(baseUri, true, "UTC");
    actualDbName = actualUri.getDatabase();

    assert actualDbName != null;
    assertEquals("demodb", actualDbName.substring(0, 6));
    assertEquals(6, actualDbName.length());
  }
}

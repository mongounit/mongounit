/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mongounit.DatasetGenerator.extractListArgumentValues;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link DatasetGeneratorTest} class is a test class for public method of {@link DatasetGenerator}
 * class.
 */
class DatasetGeneratorTest {

  @Test
  @DisplayName("extractListArgumentValues")
  void testExtractListArgumentValues() {

    List<String> values = extractListArgumentValues("DATE_TIME,OBJECT_ID");
    assertEquals("DATE_TIME", values.get(0), "1st value should be correct");
    assertEquals("OBJECT_ID", values.get(1), "2nd value should be correct");
    assertEquals(2, values.size(), "Should be 2 values in list");

    // Missing value support
    values = extractListArgumentValues("DATE_TIME,,OBJECT_ID");
    assertEquals("DATE_TIME", values.get(0), "1st value should be correct");
    assertEquals("OBJECT_ID", values.get(1), "2nd value should be correct");
    assertEquals(2, values.size(), "Should be 2 values in list");

    // Empty list support
    values = extractListArgumentValues("");
    assertEquals(0, values.size(), "Should be 0 values in list");
  }
}

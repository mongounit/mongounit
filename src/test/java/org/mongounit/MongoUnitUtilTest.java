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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mongounit.MongoUnitUtil.COMPARATOR_FIELD_NAME;
import static org.mongounit.MongoUnitUtil.assertMatches;
//import static org.mongounit.MongoUnitUtil.assertMatchesMongoUnitValue;
//import static org.mongounit.MongoUnitUtil.assertMatchesValue;
import static org.mongounit.MongoUnitUtil.combineDatasets;
import static org.mongounit.MongoUnitUtil.compare;
import static org.mongounit.MongoUnitUtil.extractMongoUnitDatasets;
//import static org.mongounit.MongoUnitUtil.extractMongoUnitValue;
import static org.mongounit.MongoUnitUtil.extractTestClassName;
//import static org.mongounit.MongoUnitUtil.generateMongoUnitValue;
import static org.mongounit.MongoUnitUtil.getFileLocations;
import static org.mongounit.MongoUnitUtil.getTestClassNamePath;
import static org.mongounit.MongoUnitUtil.retrieveDatasetFromLocations;
import static org.mongounit.MongoUnitUtil.retrieveResourceFromFile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.BsonObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;
import org.mongounit.config.MongoUnitConfig;
import org.mongounit.model.MongoUnitCollection;
import org.mongounit.model.MongoUnitDatasets;
import org.mongounit.model.MongoUnitValue;
import org.mongounit.test.AnnotatedTestClass;
import org.mongounit.test.SampleITClass;

/**
 * {@link MongoUnitUtilTest} is a test class for {@link MongoUnitUtil} class.
 */
class MongoUnitUtilTest {

  @Test
  @DisplayName("Assertion of special MongoUnit value")
  void testAssertMatchesMongoUnitValue() {

//    Map<String, Object> mongoUnitValue = new HashMap<>();
//    mongoUnitValue.put("$$", 1);
//    mongoUnitValue.put(COMPARATOR_FIELD_NAME, "=");
//
//    // = (number)
//    assertTrue(assertMatchesMongoUnitValue(mongoUnitValue, 1, "$$").isMatch(), "1 = 1");
//    assertFalse(assertMatchesMongoUnitValue(mongoUnitValue, 2, "$$").isMatch(), "1 != 2");
//
//    // > (number)
//    mongoUnitValue.put(COMPARATOR_FIELD_NAME, ">");
//    assertTrue(assertMatchesMongoUnitValue(mongoUnitValue, 0, "$$").isMatch(), "1 > 0");
//    assertFalse(assertMatchesMongoUnitValue(mongoUnitValue, 1, "$$").isMatch(), "1 !> 1");
//    assertFalse(assertMatchesMongoUnitValue(mongoUnitValue, 2, "$$").isMatch(), "1 !> 2");
//
//    // < (number)
//    mongoUnitValue.put(COMPARATOR_FIELD_NAME, "<");
//    assertTrue(assertMatchesMongoUnitValue(mongoUnitValue, 2, "$$").isMatch(), "1 < 2");
//    assertFalse(assertMatchesMongoUnitValue(mongoUnitValue, 1, "$$").isMatch(), "1 !< 1");
//    assertFalse(assertMatchesMongoUnitValue(mongoUnitValue, 0, "$$").isMatch(), "1 !< 0");
//
//    // null for expected value when actual is and is not null
//    mongoUnitValue.put(COMPARATOR_FIELD_NAME, "=");
//    mongoUnitValue.put("$$", null);
//    assertFalse(assertMatchesMongoUnitValue(mongoUnitValue, 1, "$$").isMatch(), "null != 1");
//    assertTrue(assertMatchesMongoUnitValue(mongoUnitValue, null, "$$").isMatch(), "null == null");
//
//    // null for expected value (with BSON type) when actual is and is not null
//    mongoUnitValue.put(COMPARATOR_FIELD_NAME, "=");
//    mongoUnitValue.remove("$$");
//    mongoUnitValue.put("$$DATE_TIME", null);
//    assertTrue(
//        assertMatchesMongoUnitValue(mongoUnitValue, null, "$$").isMatch(),
//        "DATE_TIME null == null");
//    mongoUnitValue.put(COMPARATOR_FIELD_NAME, "!=");
//    assertTrue(
//        assertMatchesMongoUnitValue(mongoUnitValue, "2019-10-28T16:26:10.247Z", "$$").isMatch(),
//        "DATE_TIME null != 2019-10-28T16:26:10.247Z");
//
//    // null for actual value
//    mongoUnitValue.put(COMPARATOR_FIELD_NAME, "=");
//    mongoUnitValue.put("$$", 1);
//    assertFalse(assertMatchesMongoUnitValue(mongoUnitValue, null, "$$").isMatch(), "1 != null");
//
//    // use comparator that's not supported
//    mongoUnitValue.put(COMPARATOR_FIELD_NAME, "?");
//    mongoUnitValue.put("$$", 1);
//    assertThrows(
//        MongoUnitException.class,
//        () -> assertMatchesMongoUnitValue(mongoUnitValue, 1, "$$"));
//
//    // use comparator other than = with null
//    mongoUnitValue.put(COMPARATOR_FIELD_NAME, ">");
//    mongoUnitValue.put("$$", null);
//    assertThrows(
//        MongoUnitException.class,
//        () -> assertMatchesMongoUnitValue(mongoUnitValue, 1, "$$"));
//    assertThrows(
//        MongoUnitException.class,
//        () -> assertMatchesMongoUnitValue(mongoUnitValue, null, "$$"));
//
//    // "!="
//    mongoUnitValue.put(COMPARATOR_FIELD_NAME, "!=");
//    mongoUnitValue.put("$$", 1);
//
//    // != (number)
//    assertFalse(assertMatchesMongoUnitValue(mongoUnitValue, 1, "$$").isMatch(), "1 = 1");
//    assertTrue(assertMatchesMongoUnitValue(mongoUnitValue, 2, "$$").isMatch(), "1 != 2");
//
//    // Expected not to be null (null != actual)
//    mongoUnitValue.put("$$", null);
//    assertTrue(assertMatchesMongoUnitValue(mongoUnitValue, 1, "$$").isMatch(), "null != 1");
//
//    // without a comparator (defaults to =)
//    mongoUnitValue.remove(COMPARATOR_FIELD_NAME);
//    mongoUnitValue.put("$$", 5);
//    assertTrue(assertMatchesMongoUnitValue(mongoUnitValue, 5, "$$").isMatch(), "5 = 5, assuming =");
  }

  @Test
  @DisplayName("Assertion of regular (not MongoUnit) expected map value.")
  void testAssertMatchesValueMapExpected() {

//    MongoUnitConfig props = new MongoUnitConfig(null, null, "$$mongounit$$", null, null);
//
//    // expected is map, actual is not
//    Map<String, Object> expectedValue = new HashMap<>();
//    expectedValue.put("name", "Test");
//    expectedValue.put("age", 30);
//    assertFalse(assertMatchesValue(expectedValue, 1, props).isMatch(), "Map doesn't match number.");
//    assertFalse(
//        assertMatchesValue(expectedValue, null, props).isMatch(), "Map doesn't match null.");
//    assertFalse(
//        assertMatchesValue(
//            expectedValue,
//            Arrays.asList("hello", "world"),
//            props).isMatch(),
//        "Map doesn't match list.");
//
//    // actual map
//    Map<String, Object> actualValue = new HashMap<>();
//    actualValue.put("name", "Test");
//    actualValue.put("age", 30);
//    assertTrue(
//        assertMatchesValue(
//            expectedValue,
//            actualValue,
//            props).isMatch(),
//        "Maps should match.");
//
//    // Actual map has fields that comparison is supposed to ignore since not specified in expected
//    actualValue.put("favcolor", "green");
//    assertTrue(
//        assertMatchesValue(
//            expectedValue,
//            actualValue,
//            props).isMatch(),
//        "Maps should match even if actual has more props.");
//
//    // Expected has field value different than actual
//    expectedValue.put("age", 40);
//    assertFalse(
//        assertMatchesValue(
//            expectedValue,
//            actualValue,
//            props).isMatch(),
//        "Maps should not match. Age in expected is different");
  }

  @Test
  @DisplayName("Assertion of expected list value.")
  void testAssertMatchesValueListExpected() {

//    MongoUnitConfig props = new MongoUnitConfig(null, null, "$$mongounit$$", null, null);
//
//    // expected is list, actual is not
//    List<String> expectedValue = Arrays.asList("Hello", "World");
//    assertFalse(
//        assertMatchesValue(expectedValue, 1, props).isMatch(), "List doesn't match number.");
//    assertFalse(
//        assertMatchesValue(
//            expectedValue,
//            new HashMap<>(),
//            props).isMatch(),
//        "List doesn't match map.");
//    assertFalse(
//        assertMatchesValue(expectedValue, null, props).isMatch(), "List doesn't match null.");
//
//    // Identical lists
//    List<String> actualValue = Arrays.asList("Hello", "World");
//    assertTrue(
//        assertMatchesValue(
//            expectedValue,
//            actualValue,
//            props).isMatch(),
//        "Lists should match.");
  }

  @Test
  @DisplayName("Assertion of non-list, non-map generic expected value.")
  void testAssertMatchesValueSimpleExpected() {

//    MongoUnitConfig props = new MongoUnitConfig(null, null, "$$", null, null);
//
//    // numbers
//    assertTrue(assertMatchesValue(1, 1, props).isMatch(), "1 = 1");
//    assertFalse(assertMatchesValue(1, 2, props).isMatch(), "1 != 2");
//
//    // expected number, actual is List
//    assertFalse(assertMatchesValue(1, Arrays.asList(1, 2), props).isMatch(), "Number is not List");
//    assertFalse(assertMatchesValue(null, Arrays.asList(1, 2), props).isMatch(), "null is not List");
//
//    // expected number, actual is Map
//    Map<String, Object> actualValue = new HashMap<>();
//    actualValue.put("test", 1);
//    assertFalse(assertMatchesValue(1, actualValue, props).isMatch(), "Number is not Map");
//    assertFalse(assertMatchesValue(null, actualValue, props).isMatch(), "null is not Map");
//
//    // expected = null, actual = null
//    assertTrue(assertMatchesValue(null, null, props).isMatch(), "null = null");
//
//    // expected = null, actual = 1
//    assertFalse(assertMatchesValue(null, 1, props).isMatch(), "null != 1");
//
//    // expected 1, actual = null
//    assertFalse(assertMatchesValue(1, null, props).isMatch(), "1 != null");
  }

  @Test
  @DisplayName("Compare Comparable values")
  void testCompare() {

    // numbers
    assertEquals(0, compare(1, 1));
    assertEquals(-1, compare(0, 1));
    assertEquals(1, compare(1, 0));

    // nulls
    assertEquals(0, compare(null, null));
    assertEquals(1, compare(1, null));
    assertEquals(-1, compare(null, 1));
  }

  @Test
  @DisplayName("Assert documents match.")
  void testAssertMatchesDocuments() {
//    MongoUnitConfig props = new MongoUnitConfig(null, null, "$$mongounit$$", null, null);
//
//    Map<String, Object> expectedDocument = new HashMap<>();
//    expectedDocument.put("name", "TestName");
//    expectedDocument.put("favColors", Arrays.asList("green", "red"));
//    Map<String, Object> expectedAddress = new HashMap<>();
//    expectedAddress.put("street", "123 Street Name");
//    expectedAddress.put("zipcode", 123432);
//    expectedAddress.put("city", "TestCity");
//    expectedDocument.put("address", expectedAddress);
//    expectedDocument.put("largeNumber", 123456789012L);
//
//    Map<String, Object> actualDocument = new HashMap<>();
//    actualDocument.put("name", "TestName");
//    actualDocument.put("favColors", Arrays.asList("green", "red"));
//    Map<String, Object> actualAddress = new HashMap<>();
//    actualAddress.put("street", "123 Street Name");
//    actualAddress.put("zipcode", 123432);
//    actualAddress.put("city", "TestCity");
//    actualDocument.put("address", actualAddress);
//    actualDocument.put("largeNumber", 123456789012L);
//
//    assertTrue(
//        assertMatches(
//            expectedDocument,
//            actualDocument,
//            props).isMatch(),
//        "Documents should match.");
//
//    // Actual has more props that should be ignored
//    actualDocument.put("anotherfield", "test");
//    assertTrue(
//        assertMatches(
//            expectedDocument,
//            actualDocument,
//            props).isMatch(),
//        "Documents should match.");
//
//    // Expected has field not present in actual
//    expectedDocument.put("extrafield", "test");
//    assertFalse(
//        assertMatches(
//            expectedDocument,
//            actualDocument,
//            props).isMatch(),
//        "Documents should not match. Expected has another field not in actual.");
  }

  @Test
  @DisplayName("Assert collection match.")
  void testAssertMatchesCollection() {
//    MongoUnitConfig props = new MongoUnitConfig(null, null, "$$mongounit$$", null, null);
//
//    List<Map<String, Object>> expectedDocuments = new ArrayList<>();
//    Map<String, Object> expectedDocument = new HashMap<>();
//    expectedDocument.put("name", "TestName");
//    expectedDocument.put("favColors", Arrays.asList("green", "red"));
//    Map<String, Object> expectedAddress = new HashMap<>();
//    expectedAddress.put("street", "123 Street Name");
//    expectedAddress.put("zipcode", 123432);
//    expectedAddress.put("city", "TestCity");
//    expectedDocument.put("address", expectedAddress);
//    expectedDocument.put("largeNumber", 123456789012L);
//    expectedDocuments.add(expectedDocument);
//
//    expectedDocument = new HashMap<>();
//    expectedDocument.put("name", "TestAnotherName");
//    expectedDocument.put("favColors", Arrays.asList("blue", "yellow"));
//    expectedAddress = new HashMap<>();
//    expectedAddress.put("street", "456 Street Name");
//    expectedAddress.put("zipcode", 25201);
//    expectedAddress.put("city", "AnotherTestCity");
//    expectedDocument.put("address", expectedAddress);
//    expectedDocument.put("largeNumber", 987654321234L);
//    expectedDocuments.add(expectedDocument);
//
//    MongoUnitCollection expectedCollection = MongoUnitCollection.builder()
//        .collectionName("col1")
//        .documents(expectedDocuments)
//        .build();
//
//    List<Map<String, Object>> actualDocuments = new ArrayList<>();
//    Map<String, Object> actualDocument = new HashMap<>();
//    actualDocument.put("name", "TestName");
//    actualDocument.put("favColors", Arrays.asList("green", "red"));
//    Map<String, Object> actualAddress = new HashMap<>();
//    actualAddress.put("street", "123 Street Name");
//    actualAddress.put("zipcode", 123432);
//    actualAddress.put("city", "TestCity");
//    actualDocument.put("address", actualAddress);
//    actualDocument.put("largeNumber", 123456789012L);
//    actualDocuments.add(actualDocument);
//
//    actualDocument = new HashMap<>();
//    actualDocument.put("name", "TestAnotherName");
//    actualDocument.put("favColors", Arrays.asList("blue", "yellow"));
//    actualAddress = new HashMap<>();
//    actualAddress.put("street", "456 Street Name");
//    actualAddress.put("zipcode", 25201);
//    actualAddress.put("city", "AnotherTestCity");
//    actualDocument.put("address", actualAddress);
//    actualDocument.put("largeNumber", 987654321234L);
//    actualDocuments.add(actualDocument);
//
//    MongoUnitCollection actualCollection = MongoUnitCollection.builder()
//        .collectionName("col1")
//        .documents(actualDocuments)
//        .build();
//
//    assertTrue(
//        assertMatches(
//            expectedCollection,
//            actualCollection,
//            props).isMatch(),
//        "Collections should match.");
//
//    // Actual collection has a different name
//    actualCollection = MongoUnitCollection.builder()
//        .collectionName("col3")
//        .documents(expectedDocuments)
//        .build();
//
//    assertFalse(
//        assertMatches(
//            expectedCollection,
//            actualCollection,
//            props).isMatch(),
//        "Collections should not match. Collection names are different.");
//
//    // Same name, but number of docs is different
//    actualDocument = new HashMap<>();
//    actualDocument.put("name", "TestAnotherName");
//    actualDocument.put("favColors", Arrays.asList("blue", "yellow"));
//    actualAddress = new HashMap<>();
//    actualAddress.put("street", "456 Street Name");
//    actualAddress.put("zipcode", 25201);
//    actualAddress.put("city", "AnotherTestCity");
//    actualDocument.put("address", actualAddress);
//    actualDocument.put("largeNumber", 987654321234L);
//    actualDocuments.add(actualDocument);
//
//    actualCollection = MongoUnitCollection.builder()
//        .collectionName("col1")
//        .documents(actualDocuments)
//        .build();
//
//    assertFalse(
//        assertMatches(
//            expectedCollection,
//            actualCollection,
//            props).isMatch(),
//        "Collections should not match. Actual has more docs.");
  }

  @Test
  @DisplayName("Assert collections match.")
  void testAssertMatchesCollections() {
//    MongoUnitConfig props = new MongoUnitConfig(null, null, "$$mongounit$$", null, null);
//
//    List<Map<String, Object>> expectedDocuments = new ArrayList<>();
//    Map<String, Object> expectedDocument = new HashMap<>();
//    expectedDocument.put("name", "TestName");
//    expectedDocument.put("favColors", Arrays.asList("green", "red"));
//    Map<String, Object> expectedAddress = new HashMap<>();
//    expectedAddress.put("street", "123 Street Name");
//    expectedAddress.put("zipcode", 123432);
//    expectedAddress.put("city", "TestCity");
//    expectedDocument.put("address", expectedAddress);
//    expectedDocument.put("largeNumber", 123456789012L);
//    expectedDocuments.add(expectedDocument);
//
//    expectedDocument = new HashMap<>();
//    expectedDocument.put("name", "TestAnotherName");
//    expectedDocument.put("favColors", Arrays.asList("blue", "yellow"));
//    expectedAddress = new HashMap<>();
//    expectedAddress.put("street", "456 Street Name");
//    expectedAddress.put("zipcode", 25201);
//    expectedAddress.put("city", "AnotherTestCity");
//    expectedDocument.put("address", expectedAddress);
//    expectedDocument.put("largeNumber", 987654321234L);
//    expectedDocuments.add(expectedDocument);
//
//    List<MongoUnitCollection> expectedCollections =
//        Arrays.asList(
//            MongoUnitCollection.builder()
//                .collectionName("col1")
//                .documents(expectedDocuments)
//                .build(),
//            MongoUnitCollection.builder()
//                .collectionName("col2")
//                .documents(expectedDocuments)
//                .build());
//
//    List<Map<String, Object>> actualDocuments = new ArrayList<>();
//    Map<String, Object> actualDocument = new HashMap<>();
//    actualDocument.put("name", "TestName");
//    actualDocument.put("favColors", Arrays.asList("green", "red"));
//    Map<String, Object> actualAddress = new HashMap<>();
//    actualAddress.put("street", "123 Street Name");
//    actualAddress.put("zipcode", 123432);
//    actualAddress.put("city", "TestCity");
//    actualDocument.put("address", actualAddress);
//    actualDocument.put("largeNumber", 123456789012L);
//    actualDocuments.add(actualDocument);
//
//    actualDocument = new HashMap<>();
//    actualDocument.put("name", "TestAnotherName");
//    actualDocument.put("favColors", Arrays.asList("blue", "yellow"));
//    actualAddress = new HashMap<>();
//    actualAddress.put("street", "456 Street Name");
//    actualAddress.put("zipcode", 25201);
//    actualAddress.put("city", "AnotherTestCity");
//    actualDocument.put("address", actualAddress);
//    actualDocument.put("largeNumber", 987654321234L);
//    actualDocuments.add(actualDocument);
//
//    List<MongoUnitCollection> actualCollections =
//        Arrays.asList(
//            MongoUnitCollection.builder()
//                .collectionName("col1")
//                .documents(actualDocuments)
//                .build(),
//            MongoUnitCollection.builder()
//                .collectionName("col2")
//                .documents(actualDocuments)
//                .build());
//
//    assertTrue(
//        assertMatches(
//            expectedCollections,
//            actualCollections,
//            props).isMatch(),
//        "List of collections should match.");
//
//    // Extra collections that don't exist in expected
//    actualCollections =
//        Arrays.asList(
//            MongoUnitCollection.builder()
//                .collectionName("col1")
//                .documents(actualDocuments)
//                .build(),
//            MongoUnitCollection.builder()
//                .collectionName("col2")
//                .documents(actualDocuments)
//                .build(),
//            MongoUnitCollection.builder()
//                .collectionName("col3")
//                .documents(actualDocuments)
//                .build());
//
//    assertFalse(
//        assertMatches(
//            expectedCollections,
//            actualCollections,
//            props).isMatch(),
//        "List of collections should not match. Extra collection in actual.");
//
//    // Extra collection in expected
//    expectedCollections =
//        Arrays.asList(
//            MongoUnitCollection.builder()
//                .collectionName("col1")
//                .documents(expectedDocuments)
//                .build(),
//            MongoUnitCollection.builder()
//                .collectionName("col2")
//                .documents(expectedDocuments)
//                .build(),
//            MongoUnitCollection.builder()
//                .collectionName("col3")
//                .documents(expectedDocuments)
//                .build(),
//            MongoUnitCollection.builder()
//                .collectionName("col4")
//                .documents(expectedDocuments)
//                .build());
//
//    assertFalse(
//        assertMatches(
//            expectedCollections,
//            actualCollections,
//            props).isMatch(),
//        "List of collections should not match. Extra collection in expected.");
  }

  @Test
  @DisplayName("retrieveResourceFromFile")
  void testRetrieveResourceFromFile() {

    assertNotNull(retrieveResourceFromFile(
        "org/mongounit/config/test-resource.json",
        LocationType.CLASSPATH_ROOT,
        null,
        null));

    assertNotNull(retrieveResourceFromFile(
        "/org/mongounit/config/test-resource.json",
        LocationType.CLASSPATH_ROOT,
        null,
        null));

    assertNotNull(retrieveResourceFromFile(
        "test-resource.json",
        LocationType.CLASS,
        SampleITClass.class,
        "SampleClassIT"));

    assertThrows(
        MongoUnitException.class,
        () -> retrieveResourceFromFile(
            "does-not-exist-resource.json",
            LocationType.CLASS,
            SampleITClass.class,
            "SampleClassIT"));

    assertThrows(
        MongoUnitException.class,
        () -> retrieveResourceFromFile(
            "does-not-exist-resource.json",
            LocationType.CLASS,
            null,
            null));
  }

  @Test
  @DisplayName("retrieveResourceFromFile with classpath root as default")
  void testRetrieveResourceFromFileClassPathRoot() {

    assertNotNull(
        retrieveResourceFromFile(
            "org/mongounit/config/test-resource.json",
            LocationType.CLASSPATH_ROOT,
            null,
            null));

    assertThrows(
        MongoUnitException.class, () ->
            retrieveResourceFromFile(
                "does-not-exist-resource.json",
                LocationType.CLASSPATH_ROOT,
                null,
                null));
  }

  @Test
  @DisplayName("extractMongoUnitDatasets, combineNoRepeatingCollections, extractTestClassName")
  void testExtractMongoUnitDatasets() throws Exception {

    ExtensionContext extensionContext = Mockito.mock(ExtensionContext.class);
    Mockito
        .<Class<?>>when(extensionContext.getRequiredTestClass())
        .thenReturn(AnnotatedTestClass.class);

    Mockito
        .when(extensionContext.getRequiredTestMethod())
        .thenReturn(AnnotatedTestClass.class.getMethod("someTestMethod"));

    String testClassName = extractTestClassName(AnnotatedTestClass.class);
    MongoUnitDatasets mongoUnitDatasets =
        extractMongoUnitDatasets(extensionContext, testClassName, true);

    assertTrue(mongoUnitDatasets.isAssertAnnotationPresent(), "Assertion datasets present");
    assertEquals(1, mongoUnitDatasets.getSeedWithDatasets().size(), "1 seed collection.");
    assertEquals(1, mongoUnitDatasets.getAssertMatchesDatasets().size(), "1 assert collection.");

    assertEquals(
        1,
        mongoUnitDatasets.getSeedWithDatasets().get(0).getDocuments().size(),
        "1 seed document.");
    assertEquals(
        2,
        mongoUnitDatasets.getAssertMatchesDatasets().get(0).getDocuments().size(),
        "2 assert documents.");

    mongoUnitDatasets = extractMongoUnitDatasets(extensionContext, testClassName, false);

    assertTrue(mongoUnitDatasets.isAssertAnnotationPresent(), "Assertion datasets present");
    assertEquals(1, mongoUnitDatasets.getSeedWithDatasets().size(), "1 seed collection.");
    assertEquals(0, mongoUnitDatasets.getAssertMatchesDatasets().size(), "0 assert collections.");

    assertEquals(
        2,
        mongoUnitDatasets.getSeedWithDatasets().get(0).getDocuments().size(),
        "2 seed document.");
  }

  @Test
  @DisplayName("combineDatasets and combineNoRepeatingCollections")
  void testCombineDatasets() {

//    List<Map<String, Object>> dataset1Docs = new ArrayList<>();
//    Map<String, Object> doc = new HashMap<>();
//    doc.put("firstName", "Bob");
//    dataset1Docs.add(doc);
//
//    List<Map<String, Object>> dataset2Docs = new ArrayList<>();
//    doc = new HashMap<>();
//    doc.put("firstName", "Bob");
//    dataset2Docs.add(doc);
//
//    List<Map<String, Object>> dataset3Docs = new ArrayList<>();
//    doc = new HashMap<>();
//    doc.put("firstName", "Bob");
//    dataset3Docs.add(doc);
//
//    List<Map<String, Object>> dataset4Docs = new ArrayList<>();
//    doc = new HashMap<>();
//    doc.put("firstName", "Bob");
//    dataset4Docs.add(doc);
//
//    List<MongoUnitCollection> dataset1 = Arrays.asList(
//        MongoUnitCollection.builder()
//            .collectionName("testCollection1")
//            .documents(dataset1Docs)
//            .build(),
//        MongoUnitCollection.builder()
//            .collectionName("testCollection1")
//            .documents(dataset2Docs)
//            .build());
//
//    List<MongoUnitCollection> dataset2 = Arrays.asList(
//        MongoUnitCollection.builder()
//            .collectionName("testCollection1")
//            .documents(dataset3Docs)
//            .build(),
//        MongoUnitCollection.builder()
//            .collectionName("testCollection2")
//            .documents(dataset4Docs)
//            .build());
//
//    List<MongoUnitCollection> actualDataset = combineDatasets(dataset1, dataset2);
//
//    assertEquals(2, actualDataset.size(), "2 collections should be present.");
//    assertEquals(3, actualDataset.get(0).getDocuments().size(), "col 1 should have 3 docs");
//    assertEquals(1, actualDataset.get(1).getDocuments().size(), "col 2 should have 1 doc");
  }

  @Test
  @DisplayName("retrieveDatasetFromLocations")
  void testRetrieveDatasetFromLocations() {

    List<MongoUnitCollection> mongoUnitCollections =
        retrieveDatasetFromLocations(
            new String[]{"org/mongounit/test/annotatedclass/classSeed.json"},
            LocationType.CLASSPATH_ROOT,
            null,
            null);

    assertEquals(
        "myPersonCollection",
        mongoUnitCollections.get(0).getCollectionName(),
        "Collection name should match.");
    assertEquals(1, mongoUnitCollections.get(0).getDocuments().size(), "1 document in collection");

    assertThrows(
        MongoUnitException.class,
        () -> retrieveDatasetFromLocations(
            new String[]{"classSeed.json"},
            LocationType.CLASSPATH_ROOT,
            null,
            null), "Exception should be thrown (wrong file location)");
  }

  @Test
  void testExtractMongoUnitValue() {

//    Map<String, Object> valueDoc = new HashMap<>();
//    valueDoc.put("$$DATE_TIME", "2019-10-24T18:23:26.449Z");
//    valueDoc.put("comparator", ">");
//
//    MongoUnitValue mongoUnitValue = extractMongoUnitValue(valueDoc, "$$");
//
//    assertEquals("DATE_TIME", mongoUnitValue.getBsonType(), "BsonType should be DATE_TIME");
//    assertEquals("2019-10-24T18:23:26.449Z", mongoUnitValue.getValue(), "Value should be correct");
//    assertEquals(">", mongoUnitValue.getComparatorValue(), "Comparator should be '>'");
//
//    valueDoc.remove("$$DATE_TIME");
//    valueDoc.put("$$", true);
//    valueDoc.remove("comparator");
//
//    mongoUnitValue = extractMongoUnitValue(valueDoc, "$$");
//
//    assertNull(mongoUnitValue.getBsonType(), "BsonType should be null");
//    assertEquals(true, mongoUnitValue.getValue(), "Value should be true");
//    assertNull(mongoUnitValue.getComparatorValue(), "Comparator should be null");
//
//    valueDoc.remove("$$");
//
//    assertThrows(MongoUnitException.class, () -> extractMongoUnitValue(valueDoc, "$$"));
  }

  @Test
  @DisplayName("generateMongoUnitValueDocument")
  void testGenerateMongoUnitValueDocument() {

//    String formattedDateStr = "2019-10-28T15:39:24.326Z";
//    Map<String, Object> value = generateMongoUnitValue("$$", "DATE_TIME", formattedDateStr);
//
//    assertEquals(
//        formattedDateStr,
//        value.get("$$DATE_TIME"),
//        "Map should have date value under correct key.");
//
//    value = generateMongoUnitValue("$$", "NULL", null);
//    assertNull(value.get("$$NULL"), "Map should have null value under correct key.");
//
//    BsonObjectId id = new BsonObjectId();
//    String hexId = id.getValue().toHexString();
//    value = generateMongoUnitValue("$$", "OBJECT_ID", hexId);
//    assertEquals(
//        hexId,
//        value.get("$$OBJECT_ID"),
//        "Map should have Object ID value under correct key.");

  }

  @Test
  @DisplayName("getFileLocations")
  void testGetFileLocations() throws NoSuchMethodException {

    ExtensionContext extensionContext = Mockito.mock(ExtensionContext.class);
    String[] values = {};

    String[] fileLocations =
        getFileLocations(extensionContext, values, values, true, "testclass", "-seed.json");

    String[] expected = {"testclass-seed.json"};
    assertEquals(
        expected[0],
        fileLocations[0],
        "Should default to single class name based location.");
    assertEquals(1, fileLocations.length, "File locations should only have 1 value.");

    Method method = AnnotatedTestClass.class.getMethod("someTestMethod");
    Mockito.when(extensionContext.getRequiredTestMethod()).thenReturn(method);

    fileLocations =
        getFileLocations(extensionContext, values, values, false, "testclass", "-seed.json");
    expected[0] = "someTestMethod-seed.json";
    assertEquals(
        expected[0],
        fileLocations[0],
        "Should default to single class name based location.");
    assertEquals(1, fileLocations.length, "File locations should only have 1 value.");

    values = new String[1];
    values[0] = "testFile.json";
    fileLocations =
        getFileLocations(extensionContext, values, values, false, "testclass", "-seed.json");
    expected[0] = "testFile.json";
    assertEquals(
        expected[0],
        fileLocations[0],
        "Should default to single class name based location.");
    assertEquals(1, fileLocations.length, "File locations should only have 1 value.");
  }

  @Test
  @DisplayName("getTestClassNamePath")
  void testGetTestClassNamePath() {

    String testClassNamePath = getTestClassNamePath(AnnotatedTestClass.class);
    assertEquals("/org/mongounit/test", testClassNamePath);
  }
}















/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit;

/**
 * {@link LocationType} enum specifies how 'locations' values in {@link SeedWithDataset} and {@link
 * AssertMatchesDataset} annotations should be interpreted.
 */
public enum LocationType {

  /**
   * Location should be interpreted relative to the root of the classpath.
   */
  CLASSPATH_ROOT,

  /**
   * Location should be interpreted relative to the classpath root plus the package structure along
   * with class name (or 'name' specified by {@link MongoUnitTest}), i.e., if the fully qualified
   * name of the test class is 'com.mytest.MyIT' and this value is selected, the file will be
   * searched for in the '/com/mytest/MyIT' folder relative to the root of the classpath. If
   * 'com.mytest.MyIT' class' {@link MongoUnitTest} annotation specifies 'name' of 'test1', the file
   * will be searched for in the '/com/mytest/test1' folder relative to the root of the classpath.
   */
  CLASS,

  /**
   * Location value should be treated as an absolute file path.
   */
  ABSOLUTE
}

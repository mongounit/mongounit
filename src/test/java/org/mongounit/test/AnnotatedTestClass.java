/*
 * Copyright 2019 Yaakov Chaikin (yaakov@ClearlyDecoded.com). Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package org.mongounit.test;

import org.mongounit.AssertMatchesDataset;
import org.mongounit.MongoUnitTest;
import org.mongounit.SeedWithDataset;

/**
 * Class that's used to test annotation extraction.
 */
@MongoUnitTest(name = "annotatedclass")
@SeedWithDataset("classSeed.json")
@AssertMatchesDataset("classAssert1.json")
@AssertMatchesDataset("classAssert2.json")
public class AnnotatedTestClass {

  @SeedWithDataset("methodSeed1.json")
  @SeedWithDataset("methodSeed2.json")
  @AssertMatchesDataset(additionalDataset = false)
  public void someTestMethod() {
    // do nothing, it's just for annotations
  }

}

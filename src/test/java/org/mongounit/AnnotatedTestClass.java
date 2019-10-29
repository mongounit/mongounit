package org.mongounit;

/**
 * Class that's used to test annotation extraction.
 */
@SeedWithDataset("mongounit/classSeed.json")
@AssertMatchesDataset("mongounit/classAssert1.json")
@AssertMatchesDataset("mongounit/classAssert2.json")
public class AnnotatedTestClass {

  @SeedWithDataset("mongounit/methodSeed1.json")
  @SeedWithDataset("mongounit/methodSeed2.json")
  @AssertMatchesDataset(additionalDataset = false)
  public void someTestMethod() {
    // do nothing, it's just for annotations
  }

}

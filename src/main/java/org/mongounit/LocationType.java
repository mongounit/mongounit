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
   * Location should be interpreted relative to the classpath root plus the package structure, i.e.,
   * if the package of the test class is 'com.mytest' and this value is selected, the file will be
   * searched for in the '/com/mytest' folder relative to the root of the classpath.
   */
  PACKAGE,

  /**
   * Location value should be treated as an absolute file path.
   */
  ABSOLUTE
}

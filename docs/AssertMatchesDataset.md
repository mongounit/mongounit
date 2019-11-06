---
title: AssertMatchesDataset
nav_order: 8
---

# @AssertMatchesDataset

The `@AssertMatchesDataset` annotation specifies location(s) of a JSON-based file(s) with data to match the state of the database with.

It can be used on a class, on a method, or in combination of the two, which would have a cumulative effect, i.e., the data pointed to by the method annotation would be added to the data pointed to by the class annotation.

There is also an option to trigger assertion with no additional dataset by setting this annotation's `additionalDataset` to `false`. In such a case, previously set assertion datasets (if any at all) are used. This can be used to verify that nothing exists in the database or if the dataset is coming from `@SeedWithDataset` annotation with the `reuseForAssertion` set to `true`.

If neither the target test method nor its containing target test class is annotated with this annotation, no automatic assertion takes place.

Specifying this annotation on a class or a method without providing at least a single location should cause the **mongoUnit** framework to look for a default file named `ClassName-expected.json` or `methodName-expected.json` (depending on the location of the annotation). The directory in which this file will be looked for depends on the `locationType` property of the `@AssertMatchesDataset` annotation. By default, `locationType` is `CLASS`.

If `locationType` is `CLASSPATH_ROOT`, the file will be looked for at the classpath root.

If the `locationType` is `CLASS`, the file will be looked for at the classpath root plus the package structure along with class name (or `name` specified by the `@MongoUnitTest` annotation), i.e., if the fully qualified name of the test class is `com.mytest.MyIT` and this value is selected, the file will be searched for in the `/com/mytest/MyIT` folder relative to the root of the classpath. If `com.mytest.MyIT` class' `@MongoUnitTest` annotation specifies `name` of `test1`, the file will be searched for in the `/com/mytest/test1` folder relative to the root of the classpath.

If the `locationType` is `ABSOLUTE`, the file will be looked for in the root directory, i.e ., `/`.

If the **mongoUnit** framework fails to find any of the dataset file(s), an exception will be thrown.

If the desired assertion state is an empty dataset, set `additionalDataset = false` and setting any other property on this annotation.

## Cumulative effect

Specifying multiple locations has a cumulative seeding effect, i.e., the data is sequentially combined in the order specified in the `locations` array before a match assertion is made.

The same cumulative effect applies if multiple `@AssertMatchesDataset` annotations are applied to either a method or a class. If the `@AssertMatchesDataset` annotation is applied at the class level and another one is applied at the method level, the class level datasets are taken and then combined with the method level datasets.



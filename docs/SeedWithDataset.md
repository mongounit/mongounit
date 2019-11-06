---
title: SeedWithDataset
nav_order: 7
---

# @SeedWithDataset

`@SeedWithDataset` annotation specifies location(s) of a JSON-based file(s) with data to seed the database before a target test is executed. 

It can be used on a class, on a method, or in combination of the two which would have a cumulative effect, i.e., the data pointed to by the method annotation would be added to the data pointed to by the class annotation.

Specifying this annotation on a class or a method without providing at least a single location should cause the **mongoUnit** framework to look for a default file named `ClassName-seed.json` or `methodName-seed.json` (depending on the location of the annotation). 

The directory in which this file will be looked for depends on the `locationType` property of `@SeedWithDataset`. By default, `locationType` is `CLASS`.

If `locationType` is `CLASSPATH_ROOT`, the file will be looked for at the classpath root.

If the `locationType` is `CLASS`, the file will be looked for at the classpath root plus the package structure along with class name (or `name` specified by `@MongoUnitTest`), i.e., if the fully qualified name of the test class is `com.mytest.MyIT` and this value is selected (which is the default), the file will be searched for in the `/com/mytest/MyIT` folder relative to the root of the classpath. If `com.mytest.MyIT` class' `@MongoUnitTest` annotation specifies `name` of `test1`, the file will be searched for in the `/com/mytest/test1` folder relative to the root of the classpath.

If the `locationType` is `ABSOLUTE`, the file will be looked for in the root directory, i.e ., '/'.

If the **mongoUnit** framework fails to find any of the the seed data file(s), an exception will be thrown.

*If the desired initial/seeded state is an empty database, omit this annotation altogether.*

## Cumulative effect

Specifying multiple locations has a cumulative seeding effect, i.e., the data is seeded into the database sequentially and is combined in the order specified in the `locations` array before a test method is executed.

The same cumulative seeding effect applies if multiple `@SeedWithDataset` are applied to either a method or a class. If the `@SeedWithDataset` is applied at the class level and another one is applied at the method level, the class level datasets are applied to the database first followed by the method level datasets.

---
title: Getting Started
nav_order: 3
---

# Getting Started

For a working example of an application that uses **mongoUnit** for testing, see the following demo project: [mongounit-demo2](https://github.com/mongounit/mongounit-demo2)

## Install with maven

To allow **mongoUnit** to do its magic, install it with maven by including it in your `pom.xml` dependencies.

```xml
<dependency>
  <groupId>org.mongounit</groupId>
  <artifactId>mongounit</artifactId>
  <version>1.0.0</version>
  <scope>test</scope>
</dependency>
```

**NOTE:** Currently, **mongoUnit** only supports Spring Boot projects, so your `poml.xml` has to be configured with Spring Boot.

Be sure to include `<scope>test</scope>` so **mongoUnit** only shows up in your *test* classpath.

See a complete example of [`pom.xml`](https://github.com/mongounit/mongounit-demo2/blob/master/pom.xml).

## Annotate the test class with @MongoUnitTest

```java
@SpringBootTest
@MongoUnitTest
@DisplayName("MongoPersonDaoService with MongoUnit testing framework")
public class MongoPersonDaoServiceIT {

  @Autowired
  private MongoPersonDaoService mongoPersonDaoService;
  ...
  ...
```

The `@MongoUnitTest` annotation causes a few things to happen automatically.

Be default, **mongoUnit** will create a database with URI of `mongodb://localhost:27017/mongounit-testdb_yourUserName_yyyy_MM_dd_HH_mm_ss_randomHash`. Also, now, before each test in this class, the database will be wiped by dropping all of its collections, ready to be seeded with the initial state data for the next test.

The database name is date/time stamped and randomized on purpose so multiple runs can happen in parallel without stepping on each other. For example, if the team has a single development server which is used by the Continous Integration server. In such a case, it's common that several Pull Requests are getting verified and running at the same time.

## Optinally annotate the test method with either @SeedWithDataset and/or @AssertMatchesDataset

While these annotations are optional, unless you are setting up initial database state manually with the methods from `MongoUnit` class, usually, these annotations are used.

Before any `@SeedWithDataset` annotations are placed, the database is 100% empty. While it's possible that you'd want to start with an empty state, usually, you would want to populate the database with at least some initial data.

```java
@Test
@DisplayName("Create person on a non-empty database")
@SeedWithDataset
@AssertMatchesDataset
void createPersonWithExistingData() {

  CreatePersonRequest request = CreatePersonRequest.builder()
      .name("Robert")
      .address(Address.builder().street("13 Builder St.").zipcode(12345).build())
      .favColors(Arrays.asList("blue", "white"))
      .positionName("Builder")
      .build();

  mongoPersonDaoService.createPerson(request);
}
```

Both annotations (`@SeedWithDataset` and `@AssertMatchesDataset`) rely on some JSON file(s) which contain the actual data.

By default, **mongoUnit** will look for the JSON file based on the path mirroring the fully qualified test class path (including the class name itself as the deepest directory name) together with the method name.

By default, `@SeedWithDataset` will look for a file named in the format of `methodName-seed.json` and `@AssertMatchesDataset` will look for a file named in the format of `methodName-expected.json`.

For example, if the test class annotated with `@MongoUnitTest` is `org.mongounit.demo.dao.mongo.MongoPersonDaoServiceIT`, the `@SeedWithDataset` annotation on the `createPersonWithExistingData` will look for a file located at `/org/mongounit/demo/dao/mongo/MongoPersonDaoServiceIT/createPersonWithExistingData-seed.json` and the `@AssertMatchesDataset` annotation on the same method will look for a file located at `/org/mongounit/demo/dao/mongo/MongoPersonDaoServiceIT/createPersonWithExistingData-expected.json`.

Note that both paths start with a `/`, which signifies the classpath root.

## Generate JSON snapshot of the database

**mongoUnit** comes with a utility that allows you to create the `seed.json` file from an existing database.

The usualy process is for you populate the database (either with some tool or by running a single test), inspect the database to verify it's close to the state you want and then run the **mongoUnit** provided utility to generate the JSON file representing the entire database.

You can download the [dataset generator utility](https://repo1.maven.org/maven2/org/mongounit/mongounit/1.0.0/mongounit-1.0.0-jar-with-dependencies.jar) from maven central.

To use it with its minimal customizations:

```bash
$ java -jar mongounit-1.0.0-jar-with-dependencies.jar -dbUri=mongodb://localhost:27017/yourDbName

**************************
**** JSON was written to /.../output.json
**************************
$ 
```

This utility only *reads* from the database. It does not write anything to the database.

Once the `output.json` is generated, you can place it into the proper directory (e.g., `src/test/resources/org/mongounit/demo/dao/mongo/MongoPersonDaoServiceIT/` and name it appropriately.

The same process can be followed to generate the JSON file that would represent the "post" test execution database state. Alternatively, you can take the previously generated seed file, copy it, rename it, and modify it to the state you expect the database to be after the test execution.


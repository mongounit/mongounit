---
title: What is mongoUnit?
nav_order: 1
---

![mongoUnit logo](https://mongoUnit.org/assets/images/mongoUnit-logo.png)

# What is mongoUnit?

**MAIN BENEFIT IN ONE SENTENCE:** You avoid having to write more DB persistent logic code in order to verify your other DB persistent logic code.

**mongoUnit** is a data driven Integration testing framework for Spring Boot based applications that use MongoDB for persistence. The framework enables the developer to test the data access logic with relative ease.

**mongoUnit** follows close to the same principles and is modeled after [DBUnit](http://dbunit.sourceforge.net/), so I'll paraphrase its description as follows.

**mongoUnit** is a JUnit 5 extension targeted at MongoDB-driven projects (currently supporting implementations with Spring Boot) that, among other things, puts your database into a known state between test runs. This is an excellent way to avoid the myriad of problems that can occur when one test case corrupts the database and causes subsequent tests to fail or exacerbate the damage.

**mongoUnit** has the ability to export and import your database data to and from JSON datasets.

**mongoUnit** can also help you to verify that your database data match an expected set of values.

## TL;DR - How To
1. You have some Spring Boot based MongoDB persistence logic (e.g., MyClass.java)
2. You create an intergration test class (e.g., MyClassIT.java)
3. You create a test method
4. (Optional) You create a fairly simple JSON file that populates the database either before each test method runs or before a particular method runs (MongoUnit has a helper utility to help you create it)
5. (Optional) You create a fairly simple JSON file that represents what your database *should* contain once that method runs (MongoUnit has a helper utility to help you create it)
6. (Optional) You annotate your test method with `@SeedWithDataset`. Before your test method runs, MongoUnit will automatically find the right JSON to prepopulate the DB from the file you created earlier
7. (Optional) You annotate your test method with `@AssertMatchesDataset`. After your test method runs, MongoUnit will automatically find the right JSON to compare the "after" state of the DB to that JSON
8. You can still run your own additional tests within your test method if you want/need.
9. There is a lot more, but you ARE reading the TL;DR 😉 (See [Features](https://mongounit.org/features.html) for more).

## See mongoUnit in action

If you want to see a working (demo) project that uses **mongoUnit**, take a look at [mongounit-demo2](https://github.com/mongounit/mongounit-demo2).

Here is an example of a test class from [mongounit-demo2](https://github.com/mongounit/mongounit-demo2). Take a look for yourself as to how easy it is:

```java
package org.mongounit.demo.dao.mongo; 

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mongounit.AssertMatchesDataset;
import org.mongounit.LocationType;
import org.mongounit.MongoUnit;
import org.mongounit.MongoUnitTest;
import org.mongounit.SeedWithDataset;
import org.mongounit.demo.dao.model.Address;
import org.mongounit.demo.dao.model.CreatePersonRequest;
import org.mongounit.demo.dao.model.Person;
import org.mongounit.demo.dao.model.UpdatePersonRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@MongoUnitTest(name = "personDaoService")
@DisplayName("MongoPersonDaoService with MongoUnit testing framework")
public class MongoPersonDaoServiceIT {

  @Autowired
  private MongoPersonDaoService mongoPersonDaoService;

  @Test
  @DisplayName("Create person on an empty database")
  @AssertMatchesDataset
  void createPerson() {

    CreatePersonRequest request = CreatePersonRequest.builder()
        .name("Bob The Builder")
        .address(Address.builder().street("12 Builder St.").zipcode(12345).build())
        .favColors(Arrays.asList("red", "green"))
        .positionName("Builder")
        .build();

    mongoPersonDaoService.createPerson(request);
  }

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

  @Test
  @DisplayName("Create person on a non-empty database with explicit seed")
  @SeedWithDataset("onePersonToStart.json")
  @AssertMatchesDataset("expected2People.json")
  void createPersonWithExistingDataExplicitSeed() {

    CreatePersonRequest request = CreatePersonRequest.builder()
        .name("Robert")
        .address(Address.builder().street("13 Builder St.").zipcode(12345).build())
        .favColors(Arrays.asList("blue", "white"))
        .positionName("Builder")
        .build();

    mongoPersonDaoService.createPerson(request);
  }

  @Test
  @DisplayName("Create person on a non-empty database with classpath root datasets")
  @SeedWithDataset(
      value = "common/createPersonWithExistingData-seed.json",
      locationType = LocationType.CLASSPATH_ROOT)
  @AssertMatchesDataset(
      value = "common/createPersonWithExistingData-expected.json",
      locationType = LocationType.CLASSPATH_ROOT
  )
  void createPersonWithExistingDataWithPackageRelative() {

    CreatePersonRequest request = CreatePersonRequest.builder()
        .name("Robert")
        .address(Address.builder().street("13 Builder St.").zipcode(12345).build())
        .favColors(Arrays.asList("blue", "white"))
        .positionName("Builder")
        .build();

    mongoPersonDaoService.createPerson(request);
  }

  @Test
  @DisplayName("Update person")
  @SeedWithDataset("createPersonWithExistingData-seed.json")
  @AssertMatchesDataset
  void updatePerson() {

    UpdatePersonRequest updateRequest =
        new UpdatePersonRequest(
            "5db7545b7b615c739732c777",
            "Builder",
            "Robert",
            Arrays.asList("red", "green"),
            new Address("12 Builder St.", 12345));
    mongoPersonDaoService.updatePerson(updateRequest);

    // Any other API functions can be called here and asserted as usual
    Person updatedPerson = mongoPersonDaoService.getPerson("5db7545b7b615c739732c777");
    assertTrue(
        updatedPerson.getCreated().compareTo(updatedPerson.getUpdated()) < 0,
        "Updated data should be after created date");
  }

  @Test
  @DisplayName("Update person - Manual MongoUnit Testing")
  void updatePersonManualTest() {

    // Seed with data
    MongoUnit.seedWithDataset("createPersonWithExistingData-seed.json", this.getClass());

    // Perform API action
    UpdatePersonRequest updateRequest =
        new UpdatePersonRequest(
            "5db7545b7b615c739732c777",
            "Builder",
            "Robert",
            Arrays.asList("red", "green"),
            new Address("12 Builder St.", 12345));
    mongoPersonDaoService.updatePerson(updateRequest);

    // Any other API functions can be called here and asserted as usual
    Person updatedPerson = mongoPersonDaoService.getPerson("5db7545b7b615c739732c777");
    assertTrue(
        updatedPerson.getCreated().compareTo(updatedPerson.getUpdated()) < 0,
        "Updated data should be after created date");

    // Assert database is in the expected state
    MongoUnit.assertMatchesDataset("updatePerson-expected.json", this.getClass());
  }

  @Test
  @DisplayName("Delete person")
  @SeedWithDataset("createPersonWithExistingData-seed.json")
  @AssertMatchesDataset
  void deletePersonKeepCollections() {
    mongoPersonDaoService.deletePerson("5db7545b7b615c739732c777");
  }
}
```




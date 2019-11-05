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

---
title: Requirements
nav_order: 3
---

# Requirements

* Java 17+
* Spring Boot 3+
* JUnit 5
* Tested with MongoDB 7+ (but should work with lower versions with no issues)
* Oder version support:
  * Support for Java 8 and Spring Boot 1 and 2 is available. (For Spring Boot version 3.x and above use mongoUnit 3.1.1, for Spring Boot version `2.3` and above, use mongoUnit version `2.0` and above. For earlier versions of Spring Boot, use mongoUnit version `1.1.0`.)

# Spring Boot Version Compatibility

If you are using Spring Boot version 3.3+, you must use the latest mongoUnit `3.1+`. In your `pom.xml`, you would have the following:
```
<dependency>
   <groupId>org.mongounit</groupId>
   <artifactId>mongounit</artifactId>
   <version>3.1.1</version>
   <scope>test</scope>
</dependency>
```


If you are using Spring Boot version 2.3+, you must use the latest mongoUnit `2.0+`. In your `pom.xml`, you would have the following:
```
<dependency>
   <groupId>org.mongounit</groupId>
   <artifactId>mongounit</artifactId>
   <version>2.0.1</version>
   <scope>test</scope>
</dependency>
```

If you are using earlier versions of Spring Boot, you must use mongoUnit `1.1.0`. In your `pom.xml`, you would have the following:
```
<dependency>
   <groupId>org.mongounit</groupId>
   <artifactId>mongounit</artifactId>
   <version>1.1.0</version>
   <scope>test</scope>
</dependency>
```

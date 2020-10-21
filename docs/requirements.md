---
title: Requirements
nav_order: 3
---

# Requirements

* Java 8+
* Spring Boot 2+
* JUnit 5
* Tested with MongoDB 4+ (but should work with lower versions with no issues)

# Spring Boot Version Compatibility

If you are using Spring Boot version 2.3+, you must use the latest mongoUnit 2.0+, i.e.,
```
<dependency>
   <groupId>org.mongounit</groupId>
   <artifactId>mongounit</artifactId>
   <version>2.0.0</version>
   <scope>test</scope>
</dependency>
```

If you are using earlier versions of Spring Boot, you must use mongoUnit 1.1, i.e,
```
<dependency>
   <groupId>org.mongounit</groupId>
   <artifactId>mongounit</artifactId>
   <version>1.1.0</version>
   <scope>test</scope>
</dependency>
```

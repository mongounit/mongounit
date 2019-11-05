---
title: What is mongoUnit?
nav_order: 1
---

![mongoUnit logo](https://mongoUnit.org/assets/images/mongoUnit-logo.png)

## What is mongoUnit?

**mongoUnit** is a data driven Integration testing framework for Spring Boot based applications that use MongoDB for persistence. The framework enables the developer to test the data access logic with relative ease.

**mongoUnit** follows close to the same principles and is modeled after [DBUnit](http://dbunit.sourceforge.net/), so I'll paraphrase its description as follows.

**mongoUnit** is a JUnit 5 extension targeted at MongoDB-driven projects (currently supporting implementations with Spring Boot) that, among other things, puts your database into a known state between test runs. This is an excellent way to avoid the myriad of problems that can occur when one test case corrupts the database and causes subsequent tests to fail or exacerbate the damage.

**mongoUnit** has the ability to export and import your database data to and from JSON datasets.

**mongoUnit** can also help you to verify that your database data match an expected set of values.

## Features

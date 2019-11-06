---
title: Features
nav_order: 2
---

# Features

## Zero boiler plate configuration

With **mongoUnit**, you can start testing your data access logic with placing just a few annotations. No configuration 
of *anything* is necessary.

**mongoUnit** will create a temporary database, put it into a "pre" testing state, run the test(s), check that the "post"
testing state is correct, and then drop the temporary database.

Nothing to manually clean up.

## Composable initial & expected state

Before each test, the database is seeded with JSON-based datasets. **mongoUnit** allows you to break up that JSON into
separate files so you can reuse and compose them in myriad of ways.

The same thing applies to the JSON-based files that verify the database state after your data access logic has been executed.

## “Honey, I wiped production data!” protections

Developer mistakes happen. By default, **mongoUnit** uses a testing database URI, but even if you point **mongoUnit** to a
slightly more important database server, **mongoUnit** will avoid erasing or corrupting the database. Instead, it will create
another database with a similar name. (*Obviously*, you should NEVER point your testing code to a database that has production
data, even with the **mongoUnit** protections built in.)

In the end, sure, you can still wragle **mongoUnit** to wipe your production data, but you would have to *work at it*.
In other words, making this mistake is harder than not making it.

## Utility to create DB state snapshots

**mongoUnit** comes with a utility that will export the data from the MongoDB database in a very compact and easy to read
format. Yes, this format will allow seeding the database before a test is executed and verifying its state afterwards.

## Customize behavior through property file or command-line

Be default, you don't need to customize *anything*. However, **mongoUnit** allows you a few key customizations that will
prove helpful in day-to-day development and debugging of your data access logic and your tests.

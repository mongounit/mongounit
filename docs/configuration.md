---
title: Configuration
nav_order: 6
---

# Configuration

**mongoUnit** works as is without any additional configurations. However, there are times when you may want to change the defaults to something that suites your setup better.

For example, it's often the case when debugging a test that you would NOT want **mongoUnit** to drop the database. Instead, it's useful to keep it around after the test ran to examine what the code you are targeting with the test produced in the database.

All of the **mongoUnit** configuration options can be applied on a "one-off" basis on the command line as a system property or committed as part of your codebase.

**mongoUnit** will look for `mongounit.properties` file at the root of the classpath to detect these configuration options.

For an example use of the `mongounit.properties` files, see [mongounit-demo2](https://github.com/mongounit/mongounit-demo2) project.

The following is a table of all of the configuration options:

| Option | Description |
| --------------------- | ---------------- |
| `mongounit.base-uri` or `-Dmongounit.base-uri`| If the user does not specify the `base-uri`, the default of `mongodb://localhost:27017/mongounit-testdb` is used. This URI is only used "as is" if the `mongounit.base-uri.keep-as-is` is set to true. Otherwise, the database name is changed with a one-time pad which consists of the username of the user executing the tests, followed by a  date/time stamp, followed by a hash of a UUID. |
| `mongounit.drop-database` or `-Dmongounit.drop-database` | By default (`true`), at the end of the test suite execution, drops the database, so there is no need manual cleanup. |
| `mongounit.base-uri.keep-as-is` or `-Dmongounit.base-uri.keep-as-is` | *DANGER! DANGER!* :-) Setting the following property to true will wipe the database pointed to by the value of the `mongounit.base-uri` property. *BE SURE* that it's not pointing to real data!!! By default (`false`) to make the DB URI unique per execution, appends one-time pad to the database name specified in the URI of the `mongounit.base-uri` which consists of the username of the user executing the tests, followed by a date/time stamp, followed by a hash of a random number. If set to `true`, the URI specified in `mongounit.base-uri` will be used as is (without a one-time pad). |
| `mongounit.indicator-field-name` or `-Dmongounit.indicator-field-name` | By default (`$$`) is the field name in a mongo document that indicates the document to which that field name belongs is not a regular document, but a special **mongoUnit** framework specification of either what BSON type a value is or what comparator to use when asserting a match. If the **real** data contains a field name that is named `$$`, this property allows the user to change the indicator to some other name that does **not** appear in the real data. If the dataset file is used solely for assertion, the BSON type can be omitted, i.e., `"$$": 234`. If the `"comparator"` is missing, it is assumed to have the value of `"="`. |
| `mongounit.local-time-zone-id` or `-Dmongounit.local-time-zone-id`| Specifies the local time zone. This is helpful for the logs because, by default, one of the things the **mongoUnit** framework pads the database name with is date/time. Providing the local time zone ID enables the MongoUnit framework to use that instead of the default `UTC`. For valid values for this field, please see the JavaDoc of [`ZoneId`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/ZoneId.html) class. (As an example, Eastern Standard Time or EST can be expressed as `UTC-4`). |

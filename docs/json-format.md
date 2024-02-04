---
title: Seed & Assertion JSON Formats
nav_order: 5
---

# JSON Format

## Seeding JSON format

Essentially, the seeding JSON format is simply an array of collection objects, each of which has a `collectionName` and an array of `documents`.

For example:

```json
[
{
  "collectionName": "people",
  "documents": [
  {
    "_id": { "$$OBJECT_ID": "5db7545b7b615c739732c777" },
    "name": "Bob The Builder",
    "created": {     
      "$$DATE_TIME": "2019-10-28T16:49:31.442Z"
    }
  }
  ]
},
{
  "collectionName": "positions",
  "documents": [
    {
      "positionName": "Builder",
      "_id": {
        "$$OBJECT_ID": "5db7545b7b615c739732c776"
      },
      "created": {
        "$$DATE_TIME": "2019-10-28T16:49:31.442Z"
      },
      "updated": {
        "$$DATE_TIME": "2019-10-28T16:49:31.442Z"
      }
    }
  ]
}
]
```

The above JSON contains 2 collections, one named `people` and another named `positions`. Each of the collections have 1 document.

Note that it's not necessary to specify the `_id` field if its value is insignificant. The MongoDB database will generate the value for you when this data is inserted into the database.

Outside of `OBJECT_ID` and `DATE_TIME` Bson types, it's rarely necessary to specify the BSON types explicitely and one can rely on the automatic type interpretation. For example, the `name` field is specified directly as `"name": "Bob The Builder"`. The STRING Bson type is assumed.

However, when it comes to `OBJECT_ID` and `DATE_TIME` Bson types, relying on the automatic type interpretation is usually not a good idea. It causes MongoDB and the **mongoUnit** framework to handle these as STRING data type, which is not usually good enough for interacting with its values. 

By default, the [dataset generator utility](https://mongounit.org/getting-started.html#generate-json-snapshot-of-the-database) creates all `OBJECT_ID` and `DATE_TIME` values explicitely annotated in the generated JSON file.

When you need to specify the type of a value, follow this format:
```
"fieldName": {
  "$$BSON_TYPE": value
}
```

The configurable `$$` in the type specification field name is a configuration trigger to let **mongoUnit** know that this is a special case that needs interpretation and not just a regular field in a document.

The `BSON_TYPE` is the Bson type that directly corresponds to the `enum` names/constants of the [`BsonType` class](https://mongodb.github.io/mongo-java-driver/3.11/javadoc/org/bson/BsonType.html), which is part of the MongoDB Java driver.

## Assertion JSON format

The JSON file used for assertion is (or can be) essentially in the exact same format as the seeding JSON format.

However, to facilite greater flexibility in comparison between the actual dataset and the expected dataset, there is extra syntax.

For example:

```json
[
{
  "collectionName": "positions",
  "documents": [
    {
      "_id": {
        "$$OBJECT_ID": "5db7545b7b615c739732c776"
      },
      "positionName": "Builder",
      "created": {
        "$$DATE_TIME": "2019-10-28T16:49:31.442Z"
      },
      "updated": {
        "$$DATE_TIME": "2019-10-28T16:49:31.442Z"
      }
    }
  ]
},
{
  "collectionName": "people",
  "documents": [
    {
      "_id": {
        "$$OBJECT_ID": "5db7545b7b615c739732c777"
      },
      "positionId": "5db7545b7b615c739732c776",
      "name": "Bob The Builder",
      "favColors": [
        "red",
        "green"
      ],
      "address": {
        "zipcode": 12345,
        "street": "12 Builder St."
      },
      "created": {
        "$$DATE_TIME": "2019-10-28T16:49:31.442Z"
      },
      "updated": {
        "$$DATE_TIME": "2019-10-28T16:49:31.442Z"
      }
    },
    {
      "name": "Robert",
      "positionId": "5db7545b7b615c739732c776",
      "favColors": [
        "blue",
        "white"
      ],
      "address": {
        "zipcode": 12345,
        "street": "13 Builder St."
      },
      "created": {
        "$$DATE_TIME": "2019-10-28T17:05:36.132Z",
        "comparator": "<"
      },
      "updated": {
        "$$DATE_TIME": "2019-10-28T17:05:36.132Z",
        "comparator": "<"
      }
    }
  ]
}
]
```

As you can see, when you need to specify the Bson type or how to compare the expected to the actual value, there is a special syntax.

The following assertion format specification:
 
```json
"fieldName": {
  "$$BSON_TYPE": value,
  "comparator": "="
 }
```

**This is read as: assert that value is equal to actual.**

For example:

```json
"updated": {
  "$$DATE_TIME": "2019-10-28T17:05:36.132Z",
  "comparator": "<"
}
```
 
The above is read: "Assert that `2019-10-28T17:05:36.132Z < actual value`".

The configurable `$$` in the type specification field name is a configuration trigger to let **mongoUnit** know that this is a special case that needs interpretation and not just a regular field in a document.

The `BSON_TYPE` is the Bson type that directly corresponds to the `enum` names/constants of the [`BsonType` class](https://mongodb.github.io/mongo-java-driver/3.11/javadoc/org/bson/BsonType.html), which is part of the MongoDB Java driver.

Just like when it comes to seeding the database, outside of `OBJECT_ID` and `DATE_TIME` Bson types, it's rarely necessary to specify the BSON types explicitely and one can rely on the automatic type interpretation. For example, the `name` field is specified directly as `"name": "Bob The Builder"`. The STRING Bson type is assumed.

However, when it comes to `OBJECT_ID` and `DATE_TIME` Bson types, relying on the automatic type interpretation is usually not a good idea. It causes MongoDB and the **mongoUnit** framework to handle these as STRING data type, which is not usually good enough for interacting with its values. 

In addition, the optional `comparator` field can inform how **mongoUnit** should compare the expected to the actual value.

By default, and if omitted, the `comparator` value is always `=`, but the following are all of the comparison operator values it supports:
```
=
!=
>
<
>=
<=
```

Since specification of the Bson type and the `comparator` value is optional, the following are all equivalent:

```json
"name": {
  "$$STRING": "Bob",
   "comparator": "="
}
```

```json
"name": {
  "$$": "Bob",
   "comparator": "="
}
```

```json
"name": {
  "$$": "Bob"
}
```

```json
"name": "Bob"
```

### Special rule for assertions

There is one fundamental rule that is followed when using the assertion JSON:
**If a document specified in the assertion JSON is missing a field, that field is ignored during the assertion process.**

Sometimes, we can have very high confidence in correctness of a field such that the value of verifying it is not particular great. When figuring out what value to check for is especially difficult, the risk/reward ratio can become such that it's better to ignore this field (or verify it manually in the body of the test) instead of trying to come up with an expected value that would work.

One such clear example is the `_id` field. We can have high confidence that the MongoDB database will create the proper value for this field when a new document is inserted. At the same time, trying to guess or verify the actual value that the database might come up with for the `_id` field is not worth the effect.

The `_id` field is the easiest example for this feature application, but there are certainly others. Which fields to skip assertion for is a decision that is closely tied to your particular use case.

Sometimes, simply verifying that a field value exists (i.e., `!= null`) is good enough.


## Supported Bson types

Not all of the available Bson types listed in the [`BsonType` class](https://mongodb.github.io/mongo-java-driver/3.11/javadoc/org/bson/BsonType.html) are supported, but all of the types you'd normally want to use in testing are supported.

Here is the list of the supported Bson types:
```
ARRAY
DOCUMENT
DOUBLE
STRING
BINARY
OBJECT_ID
BOOLEAN
DATE_TIME
NULL
UNDEFINED
REGULAR_EXPRESSION
DB_POINTER
JAVASCRIPT
SYMBOL
JAVASCRIPT_WITH_SCOPE
INT32
TIMESTAMP
INT64
DECIMAL128
```

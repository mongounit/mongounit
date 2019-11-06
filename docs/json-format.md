---
title: JSON Format (seed & expected)
nav_order: 4
---

# JSON Format

## Seeding format

Essentially, the seeding JSON format is simply an array of collection objects, each of which has a `collectionName` and an array of `documents`.

For example

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

When you need to specify the type of a value, follow this format:
```
"fieldName": {
  "$$BSON_TYPE": value
}
```

The `$$` in the type specification field name is a configuration trigger to let **mongoUnit** know that this is a special case that needs interpretation and not just a regular field in a document.

The `BSON_TYPE` is the Bson type that directly corresponds to the `enum` names/constant of the [`BsonType` class](https://mongodb.github.io/mongo-java-driver/3.11/javadoc/org/bson/BsonType.html), which is part of the MongoDB Java driver.




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

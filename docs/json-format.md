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
```json
"fieldName": {
  "$$BSON_TYPE": value
}
```



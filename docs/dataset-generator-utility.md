---
title: Dataset Generator
nav_order: 5
---

# Dataset Generator Utility

While it's possible to generate the seeding and assertion JSON files by hand, it's much easier to use the **mongoUnit** dataset generator utility.

The dataset generator utility is a just a JAR that you can run inside of an IDE or on the command line.

[Download the mongoUnit JSON generator utility](https://repo1.maven.org/maven2/org/mongounit/mongounit/2.0.0/mongounit-2.0.0-jar-with-dependencies.jar).

## Process

### Step 1: Populate the database with some data.

You can do this by running your code and then examining the database (either on the command line or with a tool like [Robo 3T](https://robomongo.org/) ).

The data doesn't have to be perfect. As long as the bulk of it is the way you want it, you can edit some of the details later by hand.

### Step 2: Run the following command:

```bash
$ java -jar mongounit-2.0.0-jar-with-dependencies.jar -dbUri=mongodb://localhost:27017/yourDbName

**************************
**** JSON was written to /.../output.json
**************************
$ 
```

You can now rename/edit and place the `output.json` into your source tree.

If you run the same command without giving it any arguments, it will report and error and will list all of the available options:

```bash
$ java -jar mongounit-1.0.0-jar-with-dependencies.jar 
**** ERROR: -dbUri must be specified.
**************************
* Usage: 
* java -jar mongounit-x.x.x-jar-with-dependencies.jar .jar -dbUri=mongodb://localhost:27017/test_db -collectionNames=col1,col2 -output=./output.json
*
* Individual arguments must not have any spaces between '=' and argument value or even in the argument value itself.
* '-dbUri' (required) must be a valid MongoDB URI. Must start with 'mongodb'. Can contain username/password.
* '-output' (optional) is an absolute or relative path to the file that should be created with the dataset output in JSON format. An existing file with the same name will be erased. If '-output' is specified, it MUST end with '.json'. Defaults to './output.json' if '-output' is omitted.
* '-collectionNames' (optional) comma separated list of collection to limit dataset generation to. No spaces allowed between collection names. Defaults to all collections in the database.
* '-preserveBsonTypes' (optional) comma separated list of BSON types to generate explicit MongoUnit BSON type specification for. The string types are enum names from the org.bson.BsonType. If not specified, defaults to OBJECT_ID and DATE_TIME.
* '-mongoUnitValueFieldNameIndicator' (optional) field name to use in developer JSON files to signify that a document is a representation of a special MongoUnit value. If not specified, defaults to $$.
**************************
```

## Dataset generator options

The following table presents all of the commandline arguments the dataset generator utility is able to respond to.

| Option | Required? | Description |
| --- | --- | --- |
| `-dbUri` | yes | Must be a valid MongoDB URI. Must start with `mongodb`. Can contain username/password. |
| `-output` | no |  An absolute or relative path to the file that should be created with the dataset output in JSON format. An existing file with the same name will be erased. If `-output` is specified, it MUST end with `.json`. Defaults to `./output.json` if `-output` is omitted. |
| `-collectionNames` | no | Comma separated list of collection to limit dataset generation to. No spaces allowed between collection names. Defaults to all collections in the database. |
| `-preserveBsonTypes` | no | comma separated list of BSON types to generate explicit **mongoUnit** BSON type specification for. The string types are enum names from the [`org.bson.BsonType`](https://mongodb.github.io/mongo-java-driver/3.11/javadoc/org/bson/BsonType.html). If not specified, defaults to `OBJECT_ID` and `DATE_TIME`. |
| `-mongoUnitValueFieldNameIndicator` | no | Field name to use in developer JSON files to signify that a document is a representation of a special **mongoUnit** value. If not specified, defaults to `$$`. |

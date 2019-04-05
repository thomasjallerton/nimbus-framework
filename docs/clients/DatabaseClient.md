---
id: DatabaseClient
title: Relational Database Client
sidebar_label: Relational Database Client
---

A database client allows you to obtain an active connection to a relational database. 

## Initialisation

A database client has a type parameter which should correspond to a class that is annotated with `@RelationalDatabase`

To get an instance of a `DatabaseClient` you do: 

```java
DatabaseClient client = ClientBuilder.getDatabaseClient(DatabaseClass.class);
```

In addition the serverless function from which the client is used needs to be annotated with `@UsesRelationalDatabase(DatabaseClass.class)`, to handle cloud permissions.

In this example `DatabaseClass` would be replaced with your class that you have annotated with `@RelationalDatabase`.

## DatabaseClient Methods
* `Connection getConnection()` - Returns an active connection to the database, with no active database selected.

* `Connection getConnection(String databaseName, boolean createIfNotExist)` - Returns an active connection to the database with an active database selected. Option for creating this database if it does not exist.

## Annotation Specification
### @UsesRelationalDatabase
#### Required Parameters
* `dataModel` - The class annotated with `@RelationalDatabase` that this function will access.

#### Optional Parameters
* `stages` - The stages which this function has access to the relational database


---
id: RelationalDatabase
title: Relational Database
sidebar_label: Relational Database
---

A traditional relational database deployment.

## Basic Usage
To declare a relational database a class is annotated with @RelationalDatabase. A database name needs to be provided, along with master login credentials. Finally the database language needs to be provided. 

A basic example is shown below: 
```java
@RelationalDatabase(
        name = "NimbusExampleDatabase",
        username = "master",
        password = "387tyiehfjkd7f6s8",
        databaseLanguage = DatabaseLanguage.MYSQL
)
public class ProjectDatabase {}
```

## Database Configuration
The database languages supported are:
* MySQL
* PostgreSQL,
* Oracle database,
* MariaDB,
* Microsoft SQL Server

Additionally, the database performance can be configured with the `databaseSize` parameter. The specifics will vary provider to provider, but the FREE option will always give you the free tier offering. To customise the allocated storage space the `allocatedSizeGB` parameter can be set. The minimum is 20GB.

## Secure Credentials
As it is likely you do not want the master credentials exposed in the project you can use environment variables to set the username and password. To do this you replace the username and/or password values with `${ENVIRONMENT_VARIABLE}` where ENVIRONMENT_VARIABLE is the key name of an environment variable. This environment variable needs to be available during the compile time of your code. 

An example of this is:
```java
@RelationalDatabase(
        name = "NimbusExampleDatabase",
        username = "master",
        password = "${DATABASE_PASSWORD}",
        databaseLanguage = DatabaseLanguage.MYSQL
)
public class ProjectDatabase {}
```

## Migrating Schema
The database will initially not contain any schema, so this will need to be created. To do this it is recommended that you use the `@AfterDeployment` com.nimbusframework.nimbuscore.annotation on a function to perform the creation. You can use a `RelationalDatabaseClient` to get the connection to the database and then plug that into a framework of your choice. Any deployment scripts/files must be included in the resources of your project as the `@AfterDeployment` function is deployed as a serverless function. 

An example of a complete database with a liquibase migration is:
```java
@RelationalDatabase(
        name = "NimbusExampleDatabase",
        username = "master",
        password = "387tyiehfjkd7f6s8",
        databaseLanguage = DatabaseLanguage.MYSQL
)
public class ProjectDatabase {

    @AfterDeployment
    @UsesRelationalDatabase(dataModel = ProjectDatabase.class)
    private String migrationSchema()  {
        DatabaseClient databaseClient = ClientBuilder.getRelationalDatabase(ProjectDatabase.class);
        Connection connection = databaseClient.getConnection();

        try {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new liquibase.Liquibase("resources/changelog.xml", new ClassLoaderResourceAccessor(), database);

            liquibase.update(new Contexts(), new LabelExpression());

        } catch (LiquibaseException exception) {
            return "Liquibase failed: " + exception.getLocalizedMessage();
        }
        return "Successfully migrated";
    }
}
```

## Annotation Specification
### @RelationalDatabase
#### Required Parameters
* `name` - The name of the deployed database in the cloud provider.
* `username` - Username for the master account. Can be set using environment variables.
* `password` - Password for the master account. Can be set using environment variables.
* `databaseLanguage` - Database language.

#### Optional Parameters
* `databaseSize` - Performance com.nimbusframework.nimbuscore.configuration for the database, defaults to FREE.
* `allocatedSizeGB` - Size of space to allocate in GB, default 20.
* `stages` - A list of stages that the relational database should be deployed to. 

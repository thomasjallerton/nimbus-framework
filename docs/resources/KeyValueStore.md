---
id: KeyValueStore
title: Key-Value Store
sidebar_label: Key-Value Store
---

Key-Value stores allow you to store objects based on an external key. Similar to Java `Maps<Key, Value>` where the Value is the defined object. 

## Basic Usage
The @KeyValueStore annotation on a class is used to deploy a key-value store table. Fields that you want to be included in the table must be annotated with `@Attribute`. 

This is an example of a class declared as a KeyValueStore: 

```java
@KeyValueStore(keyType = String.class)
public class ConnectionDetail {

    @Attribute
    private String username = "";

    public ConnectionDetail() {}

    public ConnectionDetail(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
```

A default constructor is required to instantiate the class when reading from the table (similar to deserializing JSON in jackson). 

If there is a field which does not have an @Attribute annotation then when it is read from the table it will be given its default value.

To interact with a KeyValueStore see [Key Value Store Client](clients/KeyValueStoreClient.md)



## Annotation Specification
### @KeyValueStore
#### Required Parameters
* `keyType` - The class type which will be used as the primary key of the table, e.g. `String.class` or `int.class`. The key type provided to the client must be the same as this type. 


#### Optional Parameters
* `keyName` - The column name for the key in the store. If not provided defaults to "PrimaryKey". 

* `tableName` - The table name found in the cloud provider. If no table name provided this defaults to the name of the class, followed by the stage. If this is set then the table name will be the one set followed by the stage name. Must be alphanumerical.

* `existingArn` - For AWS. If you want to use an exiting table not declared in this project then prove its ARN here. The class must be configured with the same schema as the existing table. 

* `readCapacityUnits` - For AWS. The allocated read capacity for the DynamoDB table."

* `writeCapacityUnits` - For AWS. The allocated write capacity for the DynamoDB table."

* `stages` - A list of stages that this resource should be deployed to. 

### @@Attribute
#### Optional Parameters

* `columnName` - The column name in the key value store. If this is not set defaults to the field name

---
id: DocumentStore
title: Document Store
sidebar_label: Document Store
---

Document Stores allow you to store objects where the primary key is defined within them. It acts similar to a Java `Set<StoredObject>` where the comparison is done on the primary key field.

## Basic Usage
The `@DocumentStore` com.nimbusframework.nimbuscore.annotation on a class is used to deploy a document store table.Fields that you want to be included in the table must be annotated. One field must be selected as the primary key and annotated with `@Key`. The others that are to be included are annotated with `@Attribute`. 

This is an example of a class declared as a DocumentStore: 

```java
@DocumentStore
public class UserDetail {

    @Key
    private String username = "";

    @Attribute
    private String fullName = "";

    public UserDetail() {}

    public UserDetail(String username, String fullName) {
        this.username = username;
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
```

A default constructor is required to instantiate the class when reading from the table (similar to deserializing JSON in jackson). 

If there is a field which does not have either a @Key or @Attribute com.nimbusframework.nimbuscore.annotation then when it is read from the table it will be given its default value.

To interact with a DocumentStore see [Document Store Client](../clients/DocumentStoreClient.md)

## Annotation Specification
### @DocumentStore
#### Optional Parameters

* `tableName` - The table name found in the cloud provider. If no table name provided this defaults to the name of the class, followed by the stage. If this is set then the table name will be the one set followed by the stage name. Must be alphanumerical.

* `existingArn` - For AWS. If you want to use an exiting table not declared in this project then prove its ARN here. The class must be configured with the same schema as the existing table. 

* `stages` - A list of stages that this resource should be deployed to. 

### @Key and @Attribute
#### Optional Parameters

* `columnName` - The column name in the document store. If this is not set defaults to the field name
---
id: KeyValueStoreFunction
title: Key-Value Store Function
sidebar_label: Key-Value Store Function
---

A Key-Value Store serverless function is triggered when a key-value store is changed, whether that is an insert, delete or update. 

## Basic Usage
A method is annotated with `@KeyValueStoreServerlessFunction`, and a parameter defining what kind of change it should be triggered with. 

Throughout this page this definition of a Key-Value Store will be used:
```java
@KeyValueStore(keyType=String.class)
public class UserDetail {

    @Attribute
    private String fullName = "";

    @Attribute
    private String email = null;

    public UserDetail() {}

    public UserDetail(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    public String getFullName() {
        return username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
```

Here is an example function for this store:
```java
public class StoreHandlers {

    @KeyValueStoreServerlessFunction(
            dataModel = UserDetail.class,
            method = StoreEventType.INSERT
    )
    public void newItem(UserDetail newItem) {
        System.out.println("New user was created! " + newItem);
    }
}
```

This function will be triggered when an item is inserted into the key-value store. As with all serverless functions, a key-value store function must be inside of a class with a default constructor available. 

## Method Details
### Parameters
There are three cases, depending on the `StoreEventType`.

`INSERT` - At most two parameters, one is a new item with the type of the key-value store, and the other is of type `StoreEvent`. For Example: 

```java
public class StoreHandlers {

    @KeyValueStoreServerlessFunction(
            dataModel = UserDetail.class,
            method = StoreEventType.INSERT
    )
    public void newItem(UserDetail newItem, StoreEvent event) {
        System.out.println("New user was created! " + newItem);
    }
}
```

`REMOVE` - At most two parameters, one is the old item that was removed from the key-value store, and the other is of type `StoreEvent`. For example: 

```java
public class StoreHandlers {

    @KeyValueStoreServerlessFunction(
            dataModel = UserDetail.class,
            method = StoreEventType.REMOVE
    )
    public void newItem(UserDetail oldItem, StoreEvent event) {
        System.out.println("User was deleted! " + oldItem);
    }
}
```

For the above two types, the order of the parameters does not matter, and one or both parameters can be dropped out.

`MODIFY` - At most three parameters, one is the old item, one is the new item, and one is of type `StoreEvent`. For example: 
```java
public class StoreHandlers {

    @KeyValueStoreServerlessFunction(
            dataModel = UserDetail.class,
            method = StoreEventType.MODIFY
    )
    public void newItem(UserDetail oldItem, UserDetail newItem, StoreEvent event) {
        System.out.println("User was changed from " + oldItem + " to " + newItem);
    }
}
```

As before all parameters can be left out if desired, but now order does matter. If there are two key-value store objects (in this case `UserDetail`) then the old item comes first and the new item comes second. If there is only one key-value store object provided then it will be the new item. Finally if all three arguments are supplied it must follow the exact order (oldItem, newItem, event), as it is above in the example. 

### Return Type
The return type is not used for anything and will be ignored in the deployed function. In local unit tests you can still access any returned value.

## Deployment Information
In AWS, due to the way key-value store functions are triggered any change will cause a function to be triggered. This means if an item is deleted, it will trigger an insert serverless function, however it will not call the method annotated (as nimbus filters it out). This is mentioned as it could lead to an unexpected number of serverless function triggers, and then inflated bills.

## Annotation Specification
### @KeyValueStoreServerlessFunction
#### Required Parameters
* `dataModel` - The class that is annotated with `@KeyValueStore`, this function will then correspond to that table. 
* `method` - What change triggers the function. Can be `INSERT`, `REMOVE`, or `MODIFY`

#### Optional Parameters
* `timeout` - How long the function is allowed to run for before timing out, in seconds. Defaults to 10.
* `memory` - The amount of memory the function runs with, in MB. Defaults to 1024.
* `stages` - The stages that the function is deployed to.
---
id: DocumentStoreClient
title: Document Store Client
sidebar_label: Document Store Client
---

A document store client allows you to interact with a document store in a manner that is similar to a `Set<>`. 

## Initialisation

A document store client has a type parameter which should correspond to a class that is annotated with `@DocumentStore`

To get an instance of a `DocumentStoreClient` you do: 

```java
DocumentStoreClient<DocumentClass> client = ClientBuilder.getDocumentStoreClient(DocumentClass.class);
```

In addition the serverless function from which the client is used needs to be annotated with `@UsesDocumentStore(DocumentClass.class)`, to handle cloud permissions.

In this example `DocumentClass` would be replaced with your class that you have annotated with `@DocumentStore`.

## DocumentStoreClient Methods

* `put(T object)` - Inserts object into the document store

* `delete(T object)` - Removes object from the table if it exists

* `deleteKey(Object keyObject)` - Removes the object corresponding to this key from the table. The key of an object is the field that is annotated with `@Key`.

* `List<T> getAll()` - Returns a list of all items in the document store. 

* `T get(Object keyObj)` - Returns the object corresponding to the key. Returns null if no object found. 

## Annotation Specification
### @UsesDocumentStore
#### Required Parameters
* `dataModel` - The class annotated with `@DocumentStore` that this function will access.

#### Optional Parameters
* `stages` - The stages which this function has access to the document store

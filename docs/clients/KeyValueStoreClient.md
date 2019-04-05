---
id: KeyValueStoreClient
title: Key-Value Store Client
sidebar_label: Key-Value Store Client
---

A key-value store client allows you to interact with a key-value store in a manner that is similar to a `Map<>`. 

## Initialisation

A key-value store client has two type parameters which should correspond to a class that is annotated with `@KeyValueStore` and a key type. The key type should match the one found on the @KeyValueStore declaration. 

To get an instance of a `KeyValueStoreClient` you do: 

```java
KeyValueStoreClient<KeyType, ValueType> client = ClientBuilder.getKeyValueStoreClient(KeyType.class, ValueType.class);
```

In addition the serverless function from which the client is used needs to be annotated with `@UsesKeyValueStore(ValueType.class)`, to handle cloud permissions.

In this example `ValueType` would be replaced with your class that you have annotated with `@KeyValueStore`.

## KeyValueStoreClient Methods    
* `put(K key, V value)` - Inserts value into the key-value store at key, replacing any value already there.

* `delete(K key)` - Removes the object corresponding to this key from the store.

* `Map<K, V> getAll()` - Returns a map of key-value of all items in the store. 

* `V get(K key)` - Returns the object corresponding to the key. Returns null if no object found. 

## Annotation Specification
### @UsesKeyValueStore
#### Required Parameters
* `dataModel` - The class annotated with `@KeyValueStore` that this function will access.

#### Optional Parameters
* `stages` - The stages which this function has access to the key-value store

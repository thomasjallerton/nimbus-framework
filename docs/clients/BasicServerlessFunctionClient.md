---
id: BasicServerlessFunctionClient
title: Basic Serverless Function Client
sidebar_label: Basic Serverless Function Client
---

A basic serverless function client allows you to invoke `@BasicServerlessFunctions`.

## Initialisation

To get a `BasicServerlessFunctionClient` do:

```java
BasicServerlessFunctionClient client = ClientBuilder.getBasicServerlessFunctionClient();
```

Additionally the serverless function using this client must be annotated with `@UsesBasicServerlessFunctionClient`, to handle cloud permissions. 

## BasicServerlessFunctionClient Methods
* `invoke(Class<? extends Object> handlerClass, String functionName)` - Invokes the basic function found in handlerClass with method name functionName. Blocking call.

* `invoke(Class<? extends Object> handlerClass, String functionName, Object param)` - Invokes the basic function found in handlerClass with method name functionName, with the given parameter. Blocking call.

* `T invoke(Class<? extends Object> handlerClass, String functionName, Class<T> responseType)` - Invokes the basic function found in handlerClass with method name functionName and returns object of type T (which should match basic function method return type). Blocking call.

* `T invoke(Class<? extends Object> handlerClass, String functionName, Object param, Class<T> responseType)` - Invokes the basic function found in handlerClass with method name functionName, with the given parameter, and returns object of type T (which should match basic function method return type). Blocking call.

* `invokeAsync(Class<? extends Object> handlerClass, String functionName)` - - Invokes the basic function found in handlerClass with method name functionName. Non-blocking call.

* `invokeAsync(Class<? extends Object> handlerClass, String functionName, Object param)` - Invokes the basic function found in handlerClass with method name functionName, with the given parameter. Non-blocking call.

## Annotation Specification
### @UsesBasicServerlessFunctionClient
* `stages` - The stages which this function has access to the basic serverless function client

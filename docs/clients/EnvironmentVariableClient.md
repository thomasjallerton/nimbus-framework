---
id: EnvironmentVariableClient
title: Environment Variable Client
sidebar_label: Environment Variable Client
---

The `EnvironmentVariableClient` allows you to access your function environment variables set with `@EnvironmentVariable`. 

## Initialisation
To get an `EnvironmentVariableClient` do:

```java
EnvironmentVariableClient client = ClientBuilder.getEnvironmentVariableClient();
```

This client will only be able to access variables which have been set using `@EnvironmentVariable` on the serverless function method. 

## EnvironmentVariableClient methods

* `boolean containsKey(String key)` - Returns true if the environment variables contains the key, false otherwise.

* `String get(String key)` - Returns the value of the environment variable specified by key, null if the key does not exist in the environment. 
---
id: EnvironmentVariables
title: Environment Variables
sidebar_label: Environment Variables
---

Environment variables can be used to provide your function with external values and variables that can change in different stages.

## Basic Usage
A function annotated with any of the `@ServerlessFunction` annotations can be annotated with the `@EnvironmentVariable` annotation. This is given the key and the value of the desired environment variable. The variable can then be accessed using the [EnvironmentVariableClient](../../clients/EnvironmentVariableClient.md). For example:

```java
public class EnvironmentVariableExample {
    
    @BasicServerlessFunction(cron="rate(1 day)", stages={"dev", "prod"})
    @EnvironmentVariable(key="EXTERNAL_URL", value="http://example-dev-url.com", stages={"dev"})
    @EnvironmentVariable(key="EXTERNAL_URL", value="${PRIVATE_PROD_URL}", stages={"prod"})
    public void connectToExternalService() {
        EnvironmentVariableClient client = ClientBuilder.getEnvironmentVariableClient();
        String url = client.get("EXTERNAL_URL");
        /* Some processing ... */
    }
}
```

This creates the basic function with the environment variable `EXTERNAL_URL` having the value `http://example-dev-url.com` in the dev environment, and a value that is taken from the local environment variables when compiling in the prod environment. This allows you to keep private information out of your repository. 

Specifically the prod value, on compilation, will be set by looking for the `PRIVATE_PROD_URL` in the local machine environment variables. 

## Annotation Specification
### @EnvironmentVariable
#### Required Parameters
* `key` - The key with which the environment variable will be accessed.

* `value` - The value that the environment variable will have in the cloud, and sometimes the local environment, depending if the `testValue` variable is set. Can be set using local environment variables.

#### Optional Parameters
* `testValue` - The value of the environment variable in local testing. Defaults to using the value in the `value` field. 

* `stages` - The stages which the function will have this environment variable.
 

---
id: BasicFunction
title: Basic Function
sidebar_label: Basic Function
---

A basic function does not rely on any other cloud resource to be triggered. It can be invoked from other functions, possibly returning some data, or triggered on a cron schedule.

## Basic Usage
A method is annotated with `@BasicServerlessFunction`. This allows it to be invoked by a [BasicServerlessFunctionClient](../../clients/BasicServerlessFunctionClient.md). 

For example:

```java
class BasicHandler {
    
    @BasicServerlessFunction
    public long getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        return cal.getTimeInMillis();
    }
}
```

This function can then be invoked with no arguments using a `BasicServerlessFunctionClient`.

As with all serverless functions, a basic function must be inside of a class with a default constructor available. 

## Method Details
### Parameters
Can have at most one parameter, a custom user type. This is deserialized using the jackson library so for more customisation annotations can be placed in the target class. 

If it is a cron function then the method should have no parameters otherwise the function will fail (as no parameters are included in the request).

### Return Type
The return type is serialized, again using jackson, and sent back to the invoker. If it is a cron function then this return type will be unused in the cloud provider, but can still be used to verify in unit tests.

## Annotation Specification
### @BasicServerlessFunction
#### Optional Parameters
* `cron` - A schedule expression of the form `cron()` or `rate()`. Specifies when to trigger the function automatically.
* `timeout` - How long the function is allowed to run for before timing out, in seconds. Defaults to 10.
* `memory` - The amount of memory the function runs with, in MB. Defaults to 1024.
* `stages` - The stages that the function is deployed to.


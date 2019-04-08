---
id: AfterDeployment
title: After Deployment Function
sidebar_label: After Deployment Function
---

After deployment functions are functions that are run once after any deployment. They are useful as they allow you to put constant data into your stores, for example setting up a test user account or creating your database schema.

## Basic Usage
A method is annotated with `@AfterDeployment`. It is then run after any file uploads after deploying the project using the nimbus-deployment plugin. 

For Example: 
```java
class AfterDeploymentFunction {
    
    @AfterDeployment
    @UsesNotificationTopic(topic = "FileUpdates")
    public String addSubscription() {
        String id = notificationClient.createSubscription(Protocol.EMAIL, "admin@nimbusframework.com");
        return "Added subscription with ID: " + id;
    }
}
```

This is an after deployment function that adds an email to a notification topic so that when notification is sent the email address is notified. 

If you are using any com.nimbusframework.nimbuscore.clients then it is required that you still use the corresponding `@UsesClient` com.nimbusframework.nimbuscore.annotation, as above. 

## Method Details
### Parameters
No parameters are allowed on an `@AfterDeployment` method. 

### Return Type
The return type is serialized using the jackson library and then displayed after the function has run in the deployment script to help determine if the function ran correctly.

## Annotation Specification
### @AfterDeployment
#### Optional Parameters
* `isTest` - If this is set to true will run after all non test after deployment functions. This is useful for example if your non test function is creating the schema and then the test function is populating with test data. Defaults to false.
* `stages` - The stages that the function is deployed to.

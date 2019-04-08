---
id: FileStorageBucketFunction
title: File Storage Function
sidebar_label: File Storage Function
---

A file storage function is triggered whenever a File Storage Bucket either has an item created or deleted. This com.nimbusframework.nimbuscore.annotation will create the bucket if it does not already exist within the project. 

## Basic Usage
A method is annotated with `@FileStorageServerlessFunction` which is provided with a bucketName and an event type. 

Here is an example of a function which is triggered when in item is added to the bucket:
```java
class FileStorageHandlers {
    @FileStorageServerlessFunction(bucketName = "ImageBucket", eventType = FileStorageEventType.OBJECT_CREATED)
    public void newObject(FileStorageEvent event) {
        System.out.println("New file added: " + event.getKey() + " with size " + event.getSize() + " bytes");
    }
}
```

The bucket name requirements are the same as they are in [@FileStorageBucket](../FileStorageBucket.md). 

As with all serverless functions, a file storage function must be inside of a class with a default constructor available. 

It is important to note that the actual file data is not an available parameter, it must be read from the file bucket using a [FileStorageClient](../../clients/FileStorageClient.md) and the file key available in the event parameter. 

## Method Details
### Parameters
The method can have at most one parameter, a `FileStorageEvent`. This event class contains the key of the file which can then be accessed using a `FileStorageClient`. If the event is an `OBJECT_DELETED` event then the size of the file will be 0. The size provided by the event is in bytes. 

### Return Type
The return type is not used for anything and will be ignored in the deployed function. In local unit tests you can still access any returned value.

## Annotation Sepecification
### @FileStorageServerlessFunction
#### Required Parameters
* `bucketName` - The name of the file storage bucket in the cloud provider. In the actual cloud provider will be appended with the stage. 
* `eventType` -  What event will trigger the function, either an `OBJECT_CREATED` or `OBJECT_DELETED`

#### Optional Parameters
* `timeout` - How long the function is allowed to run for before timing out, in seconds. Defaults to 10.
* `memory` - The amount of memory the function runs with, in MB. Defaults to 1024.
* `stages` - The stages that the function is deployed to.

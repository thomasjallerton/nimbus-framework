---
id: FileStorageClient
title: File Storage Client
sidebar_label: File Storage Client
---

A file storage client you to read and write files from a file storage bucket. 

## Initialisation

A file storage client has a String parameter which should correspond to a to the name of the bucket that will be accessed.

To get an instance of a `FileStorageClient` you do: 

```java
FileStorageClient client = ClientBuilder.getFileStorageClient(bucketName);
```

In addition the serverless function from which the client is used needs to be annotated with `@UsesFileStorageBucket(bucketName)`, to handle cloud permissions, as well as creating the bucket if it does not exist in the project. Bucket naming has the same rules as in [@FileStorageBucket](../FileStorageBucket.md). 

In this example `bucketName` would be replaced with the name of your bucket. 

## FileStorageClient Methods

* `void saveFile(String path, File file)` - Saves the file at the specified path in the bucket. If the file bucket is a static website tries to determine the `content-type` based on the file extension. 

* `void saveFile(String path, InputStream inputStream)` - Saves the given InputStream at the specified path in the bucket.

* `void saveFile(String path, String content)` - Saves the given String at the specified path in the bucket.

* `void saveFileWithContentType(String path, String content, String contentType)` - Saves the given String at the specified path in the bucket with given `content-type` 

* `void saveFileWithContentType(String path, File file, String contentType)` - Saves the given File at the specified path in the bucket with given `content-type` 

* `void saveFileWithContentType(String path, InputStream inputStream, String contentType)` - Saves the given InputStream at the specified path in the bucket with given `content-type` 

* `void deleteFile(String path)` - Deletes the file at the given path if it exists 

* `List<FileInformation> listFiles()` - Lists all the files in the bucket, including those in sub directories 

* `InputStream getFile(String path)` - Get an InputStream corresponding to the file at path, if it exists.

## Annotation Specification
### @UsesFileStorageClient
#### Required Parameters
* `bucketName` - The name of the bucket that this function will access.

#### Optional Parameters
* `stages` - The stages which this function has access to the document store

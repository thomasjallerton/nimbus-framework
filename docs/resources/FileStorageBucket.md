---
id: FileStorageBucket
title: File Storage Bucket
sidebar_label: File Storage Bucket
---

File Storage buckets allow you to store files of any type, for example images or videos. The file storage buckets can also be designated as a static website that is exposed to the public. 

## Basic Usage

To define a File Storage Bucket a class is annotated with `@FileStorageBucket`. This creates a bucket where files can be read and written. 

A basic example is shown below: 

```
@FileStorageBucket(bucketName = "imagebucket")
public class ImageBucket {}
``` 

An important thing to note for AWS is that the bucket name must be unique across the whole platform, not just for you. The file bucket created will also not be called exactly what is provided in the field, it will also be appended with the stage that was deployed. E.g. in this example, if the stage being deployed to is dev the bucket created will be called `imagebucketdev`.

## Static Website

To define a file storage bucket as a static website, the static website flag must be set to true. The website has an index file and error file that default to `index.html` and `error.html` respectively. These can be changed with the fields in the annotation. The index file is returned when the "/" path is requested. Whenever an error occurs, for example a file is not found, then the error page is returned. 

The path of a resource in the website is relative to its path in the file system. For example, if there is a file `examplefile.html` at the top level of the file bucket, then the URL to access this resource is `http://example-url.com/examplefile.html`. Similarly if there is a file within a directory of the file bucket, `directory/examplefile.html`, then this will be found at `http://example-url.com/directory/examplefile.html`.

When a file storage bucket is created as a website, then once a deployment is completed the URL of the website will be reported.

An example usage to create a static website is shown below:

```
@FileStorageBucket(
        bucketName = "NimbusExampleWebsite",
        staticWebsite = true
)
public class Website {}
```

## File Uploads

File uploads can be done using the FileStorageBucketClient class, however this can only be done from serverless functions. If you want to upload files from your local machine then the `@FileUpload` annotation can be used. This allows you to specify files or directories to be uploaded to the bucket after it has been created. Specifically it is run on every deployment, after creation and before any `@AfterDeployment` scripts. These will trigger any functions set up to listen to the FileStorageBucket.

If a path to a file is provided, then it will be uploaded to the path specified in the target path exactly, i.e. target path should be a file like "helloworld.html".

If a path to a directory is a provided then each file in the directory (and all sub-directories) will be uploaded to the target path directory, and creating any necessary subdirectories. I.e. target path should be a directory like "" or "resources/".

The content-type used for a file when hosted in a static website is determined automatically by looking at the file uploaded. This means if a HTML file is uploaded then it is likely to be hosted with a content-type of `text/html`, however is not guaranteed to work.

An example file storage bucket, with file uploads is shown below:
```
@FileStorageBucket(
        bucketName = Website.WEBSITE_BUCKET,
        staticWebsite = true
)
@FileUpload(bucketName = Website.WEBSITE_BUCKET,
            localPath = "src/website",
            targetPath = "",
            substituteNimbusVariables = true)
public class Website {
    public static final String WEBSITE_BUCKET = "NimbusExampleWebsite";
}
```

The website you host may also want to connect to other resources created by the deployment, like a REST API, a WebSocket API, or even another file storage bucket. The URLs required to connect to these are not available until a deployment is completed, so you cannot insert these manually without doing an initial deployment. To solve this problem, nimbus can perform variable substitution to insert these URLs. 

The variables that can be substituted are:
* `${NIMBUS_REST_API_URL}` - Returns the base URL for a serverless function REST API (one created by `@HttpServerlessFunction` annotations). Does not have a "/" at the end, e.g. `http://www.target-invokation.com`. 
* `${NIMBUS_WEBSOCKET_API_URL}` - Returns the base URL for a serverless function WebSocket API (one create by `@WebSocketServerlessFunction` annotations) Does not have a "/" at the end, e.g. `http://www.websocket-invokation.com`. 
* `${BUCKETNAME_URL}` - where BUCKETNAME is the name of the bucket, as supplied in the annotation entirely uppercase. This means it does not have the stage appended to the end. This returns the URL of a file storage bucket that is configured as a static website. Does not have a "/" at the end. 

## @FileStorageBucket
#### Required Parameters
* `bucketName` - The name of the file storage bucket in the cloud provider. In the actual cloud provider will be appended with the stage. 

#### Optional Parameters
* `staticWebsite` - Whether to deploy as a static website, making the bucket publicly accessible for reads. Defaults to false.
* `indexFile` - The file to be returned when the base url is accessed. Only used in static websites. Defaults to "index.html" 
* `errorFile` - The file to be returned when an error occurs. Only used in static websites. Defaults to "error.html" 
* `stages` - The stages that this file storage bucket should be deployed to. 

## @FileUpload
#### Required Parameters
* `bucketName` - The name of the bucket to be uploaded to, as specified in the @FileStorageBucket annotation
* `localPath` - Path to file or directory on the local system
* `targetPath` - Path to file or directory where file(s) will be uploaded

#### Optional Parameters
* `substituteNimbusVariables` - If variables should be substituted in the local file(s). Default false.
* `stages` - The stages that the files will be uploaded. 


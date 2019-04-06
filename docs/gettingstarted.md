---
id: gettingstarted
title: Getting Started
sidebar_label: Getting Started
---

## Installation

Nimbus is made available though maven and is hosted in the maven central repository.

First you need to added the nimbus-core dependency to your pom. This adds the annotation processor and local testing capabilities.
```xml
<dependency>
    <groupId>com.allerton</groupId>
    <artifactId>nimbus</artifactId>
    <version>0.3</version>
</dependency>
```

Next the deployment plugin needs to be added. Currently only AWS is supported, so the region parameter needs to be supplied to the plugin configuration.
```xml
<plugin>
    <groupId>com.allerton</groupId>
    <artifactId>nimbus-deployment-maven-plugin</artifactId>
    <version>0.1</version>
    <configuration>
        <region>eu-west-1</region>
    </configuration>
</plugin>
```

You also need to use the maven shade plugin to package your project into a fat jar:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.2.1</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <outputFile>target/lambda.jar</outputFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```
Here the compiled jar will be placed at `target/lambda.jar`

Finally, an optional step is to create a `nimbus.yml` file in the top level of your project. This has only one parameter, `projectName`, that lets you customise the project name. For example:
```yaml
projectName: nimbusExample
```

Caution: projectName needs to be alphanumeric.

## Deployment Plugin Configuration Parameters
* `region` - The AWS [region](https://docs.aws.amazon.com/general/latest/gr/rande.html) that this project will be deployed to.

* `stage` - The project stage that will be deployed to

* `shadedJarPath` - The location where the fat jar is found. Defaults to `target/lambda.jar`. 

* `compiledSourcePath` - The location where compiled nimbus files are found. This will be in the compiled annotation sources. Defaults to `target/generated-sources/annotations/` (Correct for default java projects, for other languages will likely change)

## How to Deploy
To deploy to AWS you need to provide your AWS credentials. This can be done in three ways:

* Environment variables–AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY. The AWS SDK for Java uses the EnvironmentVariableCredentialsProvider class to load these credentials.

* Java system properties–aws.accessKeyId and aws.secretKey. The AWS SDK for Java uses the SystemPropertiesCredentialsProvider to load these credentials.

* The default credential profiles file– typically located at ~/.aws/credentials (location can vary per platform). For example: 
```txt
[default]
aws_access_key_id = AKIAEXAMPLEKEY
aws_secret_access_key = s8sjhdf/sifys+EXAMPLEACCESSKEY
```

Then to deploy you need to package your project by running `mvn package`, and then deploy using `mvn nimbus-deployment:deploy`

This creates a new stack, and reports any new endpoints.

## Sample application
Here we show the deployment of a simple HelloWorld application (assuming correct pom and nimbus.yml files)

```java
public class HelloWorld {
    
    @HttpServerlessFunction(path="helloWorld", method=HttpMethod.GET)
    public String helloWorld() {
        return "Hello World!";
    }
}
```

Now, we run `mvn package`.

After this we run `mvn nimbus-deployment:deploy`. On a successful run we get this output:

```txt
[info] Beginning deployment for project: HelloWorld, stage: dev
[info] Creating stack
[info] Polling stack create progress
***************
[info] Stack created
[info] Uploading lambda file
[info] Uploaded file
[info] Uploading cloudformation file
[info] Uploaded file
[info] Updating stack
**********************
[info] Updated stack successfully, deployment complete
[info] Deployment completed
[info] Created REST API. Base URL is https://bq0za24ki8.execute-api.eu-west-1.amazonaws.com/dev
```

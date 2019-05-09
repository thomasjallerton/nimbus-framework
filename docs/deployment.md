---
id: Deployment
title: Deployment
sidebar_label: Deployment
---

## Cloud Providers
### AWS
Currently only AWS is supported and to deploy AWS credentials are required. 

This can be done in three ways:

* Environment variables–AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY. The AWS SDK for Java uses the EnvironmentVariableCredentialsProvider class to load these credentials.

* Java system properties–aws.accessKeyId and aws.secretKey. The AWS SDK for Java uses the SystemPropertiesCredentialsProvider to load these credentials.

* The default credential profiles file– typically located at ~/.aws/credentials (location can vary per platform). For example: 
```txt
[default]
aws_access_key_id = AKIAEXAMPLEKEY
aws_secret_access_key = s8sjhdf/sifys+EXAMPLEACCESSKEY
```

## Deployment Plugins
There are two main ways to deploy functions using Nimbus. Firstly all the functions can be packaged together using the maven shade plugin into one large jar, and secondly each function can be packaged into separate, smaller JARs. Regardless of which method is chosen, the nimbus deployment plugin is required.

```xml
<plugin>
    <groupId>com.nimbusframework</groupId>
    <artifactId>nimbus-deployment-maven-plugin</artifactId>
    <version>0.6</version>
    <configuration>
        <region>eu-west-1</region>
        <stage>dev</stage>
    </configuration>
</plugin>
``` 
#### Deployment Plugin Configuration Parameters
* `region` - The AWS [region](https://docs.aws.amazon.com/general/latest/gr/rande.html) that this project will be deployed to.

* `stage` - The project stage that will be deployed to

* `shadedJarPath` - The location where the shaded jar is found (if using maven shade plugin). Defaults to `target/functions.jar`. 

* `compiledSourcePath` - The location where compiled nimbus files are found. This will be in the compiled annotation sources. Defaults to `target/generated-sources/annotations/` (Correct for default java projects, for other languages will likely change)


### Using Maven Shade Plugin
When the maven shade plugin is used all the functions in the project are compiled into one large JAR. 
#### Advantages & Disadvantages
The main advantage of the maven shade plugin is that all the required dependencies are guaranteed to be present in the shaded JAR (assuming POM configured correctly). 

The disadvantages are that if there are many functions in the project, each with separate dependencies, then the function jar can end up being very large. This can lead to slower cold start times, but most importantly many cloud providers have a JAR size limit (e.g. AWS has a 50 MB limit). Thus once this limit has been reached no more deployments can be done, until the project is refactored so that is built in separate chunks. 

#### Usage
The maven shade plugin needs to be added to the POM:
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
                <outputFile>target/functions.jar</outputFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

This configuration will run the shade goal during the package phase. If the `outputFile` parameter is changed from the above then the `shadedJarPath` in the deployment plugin must also be changed. To deploy run `mvn package` and then `mvn nimbus-deployment:deploy`.  

### Using Nimbus Assembly
When Nimbus assembly is used, each function is compiled into it's own JAR that contain only that functions dependencies. This is done by analysing the dependencies of the function, and only including those dependencies. To avoid most issues found from reflection and loading classes directly, when a class dependency is found the entire artifact that class is contained in is also added into the JAR. However this is not perfect, notably in cases where classes not found in a project are attempted to be loaded in via reflection, though this case is rare. 
#### Advantages & Disadvantages
The advantages of using nimbus assembly is that the functions will be much smaller and therefore have a faster cold start time, and be much less likely to hit the function size limit. 

The main disadvantage is that the function now has a chance to fail, and so will need to be well tested in the cloud environment. A smaller disadvantage is that the deployment can take longer if all functions need to be assembled, as file I/O and uploading each separate function becomes a bottleneck. To reduce the impact of this nimbus can detect the functions that have changed and only assemble and upload those. 

#### Usage
No additional plugins need to be added to the POM. The `nimbus.yml` file needs to be modified by adding a new parameter `assemble`, e.g.
```yml
#nimbus.yml
projectName: ExampleProject
assemble: true
```

To deploy run `mvn compile` and then `mvn nimbus-deployment:deploy`.  
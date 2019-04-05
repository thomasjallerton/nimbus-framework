---
id: HttpFunction
title: HTTP Function
sidebar_label: HTTP Function
---

HTTP functions are used to build up a REST api for your project. You can only have one REST api in a nimbus project (i.e. there will be only one endpoint). 

## Basic Usage
To configure a HTTP function, you annotate a method with `@HttpServerlessFunction` and provide it a unique path and method. This function will be triggered when a request is made that matches this path and method. 

A basic example is shown below:
```java
class RestHandlers {

    @HttpServerlessFunction(method = HttpMethod.GET, path = "example/path")
    public String helloWorldExample() {
        return "HELLO_WORLD";
    }
    
}
```

As with all serverless functions, a HTTP function must be inside of a class with a default constructor available. 

## Method Details
### Parameters
A HTTP serverless function method can have at most two parameters. One of these is a `HttpEvent` type, which contains the header, path and query parameters. The second method parameter available is a custom user type which will be read and deserialized from the JSON body of the request. For example `String` can be used to read the body in as a string. This is done using the jackson library, so any customisation you want can be done using jackson annotations in the target class. 

Here is an example using both of these parameters:
```java
class RestHandlers {
   
    @HttpServerlessFunction(method = HttpMethod.POST, path = "person")
    public String sendPerson(Person person, HttpEvent event) {
        String log = event.getQueryStringParameters().get("log");
        if (log != null && log.equals("true")) {
            System.out.println(person);
        }
        return "Got person in request! " + person.toString();
    }
    
    class Person {
        public String firstName;
        public String lastName;
    
        @Override
        public String toString() {
            return "Person{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    '}';
        }
    }
}
```

To trigger this function then we send a POST request to BASE_URL/person with a body similar to: 
```json
{
   "firstName": "Thomas",
   "lastName": "Smith"
}
```

The order of the parameters supplied to the method does not matter, and one or both can be left out.

### Return parameters
#### Simple Behaviour
The default behaviour of the function is to return a 200 status code if the method returns successfully, with a body which is the serialised value of whatever was returned. If the method throws an exception then the function will return a 500 status code, stating a server error. In the hello world example above a `String` return type is specified. If a HTTP request is sent to this function the response will be a JSON string like `"HELLO_WORLD"`. 

This is an example with a more complicated return type (Using previous person class):
```java
class RestHandlers {
   
    @HttpServerlessFunction(method = HttpMethod.GET, path = "person")
    public Person sendPerson() {
        Person newPerson = new Person();
        newPerson.firstName = "Thomas";
        newPerson.lastName = "Smith";
        return newPerson;
    }
}
```

This HTTP function will return with this body and status code 200:
```json
{
   "firstName": "Thomas",
   "lastName": "Smith"
}
```

#### Custom Behavior
In some cases it is likely you will want to set your own status codes and other response parameters. In this case you can set your method return type to `HttpResponse`. This class allows you to set the status code and headers yourself. It also requires that the body is also set manually, though there is are helper functions that can perform the deserialization. 

Here is an example of this in practice: 
```java
class RestHandlers {
   
    @HttpServerlessFunction(method = HttpMethod.POST, path = "people")
    public HttpResponse addPeople(List<Person> people) {
        return new HttpResponse().withBody("Not Implemented").withStatusCode(501);
    }
}
```

## Deployment Information
Many of these methods can be written to build up your REST api. Once it has been deployed the base URL will be reported. 

Any logging lines or print statements will appear in the cloud providers log service (e.g. AWS CloudWatch).

## Annotation Specification
### @HttpServerlessFunction
#### Required Parameters
* `path` - The path where the function should be triggered with, relative to the base url. e.g. for a url along the lines of `https://invokation.com/person` the path should be set to `person`.
* `method` - The HTTP method which triggers the function, e.g. `POST`, `GET` or can be catch all with `ANY`.

#### Optional Parameters
* `timeout` - How long the function is allowed to run for before timing out, in seconds. Defaults to 10.
* `memory` - The amount of memory the function runs with, in MB. Defaults to 1024.
* `stages` - The stages that the function is deployed to.
package handlers;

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.eventabstractions.HttpEvent;
import com.nimbusframework.nimbuscore.eventabstractions.HttpResponse;
import models.Person;
import models.NestedPerson;
import models.People;


import java.util.List;

public class HttpHandlers {

  @HttpServerlessFunction(method = HttpMethod.GET, path = "HELLOWORLD")
  public HttpResponse customResponse(HttpEvent event) {
    return new HttpResponse().withBody("HELLOWORLD").withStatusCode(200);
  }

  @HttpServerlessFunction(method = HttpMethod.POST, path = "person/path")
  public String postPath(HttpEvent event, Person person) {
    return "Success";
  }

  @HttpServerlessFunction(method = HttpMethod.POST, path = "testing")
  public void voidReturn() {
    System.out.println("LOGGED");
  }

  @HttpServerlessFunction(method = HttpMethod.POST, path = "person/path/new")
  public Person postNewPath(HttpEvent event) {
    return new Person();
  }

  @HttpServerlessFunction(method = HttpMethod.GET, path = "person")
  public List<Person> genericResponse(HttpEvent event) {
    return List.of(new Person());
  }

  @HttpServerlessFunction(method = HttpMethod.PUT, path = "person")
  public void genericInput(List<Person> people, HttpEvent event) {
    return;
  }

  @HttpServerlessFunction(method = HttpMethod.PUT, path = "nestedperson")
  public void nestedInput(NestedPerson nestedPerson, HttpEvent event) {
    return;
  }

  @HttpServerlessFunction(method = HttpMethod.GET, path = "nestedperson")
  public NestedPerson nestedResponse(HttpEvent event) {
    return new NestedPerson();
  }

  @HttpServerlessFunction(method = HttpMethod.PUT, path = "listnestedperson")
  public void listNestedInput(People people) {
    return;
  }

  @HttpServerlessFunction(method = HttpMethod.GET, path = "listnestedperson")
  public People listNestedResponse() {
    return new People();
  }

}

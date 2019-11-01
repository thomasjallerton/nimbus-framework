package handlers;

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.eventabstractions.HttpEvent;
import com.nimbusframework.nimbuscore.eventabstractions.HttpResponse;
import models.Person;

public class HttpHandlers {

  @HttpServerlessFunction(method = HttpMethod.GET, path = "HELLOWORLD")
  public HttpResponse customResponse(HttpEvent event) {
    return new HttpResponse().withBody("HELLOWORLD").withStatusCode(200);
  }

  @HttpServerlessFunction(method = HttpMethod.POST, path = "person")
  public String postPath(HttpEvent event, Person person) {
    return "Success";
  }

  @HttpServerlessFunction(method = HttpMethod.POST, path = "testing")
  public void voidReturn() {
    System.out.println("LOGGED");
  }
}

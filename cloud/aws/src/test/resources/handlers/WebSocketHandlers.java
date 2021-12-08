package handlers;

import com.nimbusframework.nimbuscore.annotations.function.WebSocketServerlessFunction;
import com.nimbusframework.nimbuscore.eventabstractions.WebSocketEvent;
import models.Person;

public class WebSocketHandlers {

  @WebSocketServerlessFunction(topic="newUser")
  public void newUser(Person newUser, WebSocketEvent event) {
    String connectionId = event.getRequestContext().getConnectionId();
    System.out.println("ConnectionId " + connectionId + " registered as new user: " + newUser);
  }

  @WebSocketServerlessFunction(topic = "$connect")
  public void onConnect(WebSocketEvent event) throws Exception {
    String connectionId = event.getRequestContext().getConnectionId();
    String username = event.getQueryStringParameters().get("user");
  }

}

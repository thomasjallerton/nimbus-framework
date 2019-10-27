package handlers;

import com.nimbusframework.nimbuscore.annotations.function.NotificationServerlessFunction;
import com.nimbusframework.nimbuscore.eventabstractions.NotificationEvent;
import models.Person;

public class NotificationHandlers {

  @NotificationServerlessFunction(topic = "TestTopic")
  public void handleNotification(NotificationEvent event, Person person) {
    System.out.println(person.getName());
  }

}
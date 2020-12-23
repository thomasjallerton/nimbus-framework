package handlers;

import com.nimbusframework.nimbuscore.annotations.deployment.CustomFactory;
import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction;
import com.nimbusframework.nimbuscore.eventabstractions.BasicEvent;
import java.util.Calendar;
import models.Person;

@CustomFactory(CustomFactoryFactory.class)
public class CustomFactoryHandler {

  private final String message;

  public CustomFactoryHandler(String message) {
    this.message = message;
  }

  @BasicServerlessFunction()
  public String getMessage() {
    return message;
  }

}

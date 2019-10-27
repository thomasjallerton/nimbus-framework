package handlers;

import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction;
import com.nimbusframework.nimbuscore.eventabstractions.BasicEvent;
import java.util.Calendar;
import models.Person;

public class BasicHandlers {

  @BasicServerlessFunction
  public long getCurrentTime() {
    Calendar cal = Calendar.getInstance();
    return cal.getTimeInMillis();
  }

  @BasicServerlessFunction
  public Person withParameter(Person person, BasicEvent event) {
    String requestId = event.getRequestId();
    return person;
  }

  @BasicServerlessFunction
  public void withPrimitiveInput(int count, BasicEvent event) {
    String requestId = event.getRequestId();
  }

}

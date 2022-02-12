package handlers;

import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction;
import com.nimbusframework.nimbuscore.eventabstractions.BasicEvent;
import java.util.Calendar;
import models.Person;

public class BasicHandlers {

  @BasicServerlessFunction(cron = "* * * 10")
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

  @BasicServerlessFunction
  public Person justReturn(BasicEvent event) {
    return new Person();
  }

  @BasicServerlessFunction
  public void justInput(Person person) {
    return;
  }

}

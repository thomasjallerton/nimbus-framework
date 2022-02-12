package handlers;

import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction;
import com.nimbusframework.nimbuscore.eventabstractions.BasicEvent;
import com.nimbusframework.nimbuscore.annotations.deployment.CustomFactoryInterface;
import java.util.Calendar;
import models.Person;

public class CustomFactoryWrongFactory implements CustomFactoryInterface<String> {

  @Override
  public String create() {
    return "message";
  }

}

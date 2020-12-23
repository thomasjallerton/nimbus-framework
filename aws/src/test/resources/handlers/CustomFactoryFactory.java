package handlers;

import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction;
import com.nimbusframework.nimbuscore.eventabstractions.BasicEvent;
import com.nimbusframework.nimbuscore.annotations.deployment.CustomFactoryInterface;
import java.util.Calendar;
import models.Person;

public class CustomFactoryFactory implements CustomFactoryInterface<CustomFactoryHandler> {

  @Override
  public CustomFactoryHandler create() {
    return new CustomFactoryHandler("message");
  }

}

package handlers;

import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction;
import com.nimbusframework.nimbuscore.eventabstractions.BasicEvent;
import com.nimbusframework.nimbuscore.annotations.deployment.CustomFactoryInterface;
import java.util.Calendar;
import models.Person;

public class CustomFactoryFactoryNoEmptyConstructor implements CustomFactoryInterface<CustomFactoryHandlerNoEmptyConstructor> {

  @Override
  public CustomFactoryHandlerNoEmptyConstructor create() {
    return new CustomFactoryHandlerNoEmptyConstructor("message");
  }

}

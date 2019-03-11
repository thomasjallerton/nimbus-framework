package annotation.annotations.function;

import annotation.annotations.NimbusConstants;
import annotation.annotations.function.repeatable.NotificationServerlessFunctions;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(NotificationServerlessFunctions.class)
public @interface NotificationServerlessFunction {
    public String topic();
    int timeout() default 10;
    int memory() default 1024;
    String[] stages() default {NimbusConstants.stage};
}

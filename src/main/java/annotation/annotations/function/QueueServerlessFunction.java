package annotation.annotations.function;

import annotation.annotations.NimbusConstants;
import annotation.annotations.function.repeatable.QueueServerlessFunctions;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(QueueServerlessFunctions.class)
public @interface QueueServerlessFunction {
    int batchSize();
    String id();
    int timeout() default 10;
    int memory() default 1024;
    String[] stages() default {NimbusConstants.stage};
}
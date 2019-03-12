package annotation.annotations.function;

import annotation.annotations.NimbusConstants;
import annotation.annotations.function.repeatable.DocumentStoreServerlessFunctions;
import annotation.annotations.persistent.StoreEventType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DocumentStoreServerlessFunctions.class)
public @interface DocumentStoreServerlessFunction {
    Class<?> dataModel();
    StoreEventType method();
    int timeout() default 10;
    int memory() default 1024;
    String[] stages() default {NimbusConstants.stage};
}

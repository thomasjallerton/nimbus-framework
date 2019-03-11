package annotation.annotations.function;

import annotation.annotations.NimbusConstants;
import annotation.annotations.function.repeatable.KeyValueStoreServerlessFunctions;
import annotation.annotations.persistent.StoreUpdate;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(KeyValueStoreServerlessFunctions.class)
public @interface KeyValueStoreServerlessFunction {
    Class<?> dataModel();
    StoreUpdate method();
    int timeout() default 10;
    int memory() default 1024;
    String[] stages() default {NimbusConstants.stage};
}

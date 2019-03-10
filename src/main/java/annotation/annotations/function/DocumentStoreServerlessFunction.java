package annotation.annotations.function;

import annotation.annotations.function.repeatable.DocumentStoreServerlessFunctions;
import annotation.annotations.persistent.StoreUpdate;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DocumentStoreServerlessFunctions.class)
public @interface DocumentStoreServerlessFunction {
    Class<?> dataModel();
    StoreUpdate method();
    int timeout() default 10;
    int memory() default 1024;
    String stage() default "dev";
}

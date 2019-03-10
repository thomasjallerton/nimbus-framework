package annotation.annotations.function;

import annotation.annotations.persistent.StoreUpdate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KeyValueStoreServerlessFunction {
    Class<?> dataModel();
    StoreUpdate method();
    int timeout() default 10;
    int memory() default 1024;
    String stage() default "dev";
}

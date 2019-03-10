package annotation.annotations.function;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueueServerlessFunction {
    int batchSize();
    String id();
    int timeout() default 10;
    int memory() default 1024;
    String stage() default "dev";
}
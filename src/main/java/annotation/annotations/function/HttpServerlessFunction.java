package annotation.annotations.function;

import annotation.annotations.function.repeatable.HttpServerlessFunctions;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(HttpServerlessFunctions.class)
public @interface HttpServerlessFunction {
    HttpMethod method();
    String path();
    int timeout() default 10;
    int memory() default 1024;
    String stage() default "dev";
}

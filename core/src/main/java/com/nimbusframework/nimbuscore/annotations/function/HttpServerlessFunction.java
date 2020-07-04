package com.nimbusframework.nimbuscore.annotations.function;

import com.nimbusframework.nimbuscore.annotations.function.repeatable.HttpServerlessFunctions;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(HttpServerlessFunctions.class)
public @interface HttpServerlessFunction {
    HttpMethod method();
    String path();
    int timeout() default 10;
    int memory() default 1024;
    String allowedCorsOrigin() default "";
    String[] allowedCorsHeaders() default {};
    String[] stages() default {};
}

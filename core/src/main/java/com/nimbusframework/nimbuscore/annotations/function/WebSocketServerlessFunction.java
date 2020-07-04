package com.nimbusframework.nimbuscore.annotations.function;

import com.nimbusframework.nimbuscore.annotations.function.repeatable.WebSocketServerlessFunctions;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(WebSocketServerlessFunctions.class)
public @interface WebSocketServerlessFunction {
    String topic();
    int timeout() default 10;
    int memory() default 1024;
    String[] stages() default {};
}

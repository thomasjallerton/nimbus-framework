package com.nimbusframework.nimbuscore.annotation.annotations.function;

import com.nimbusframework.nimbuscore.annotation.annotations.NimbusConstants;
import com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.WebSocketServerlessFunctions;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(WebSocketServerlessFunctions.class)
public @interface WebSocketServerlessFunction {
    String topic();
    int timeout() default 10;
    int memory() default 1024;
    String[] stages() default {NimbusConstants.stage};
}

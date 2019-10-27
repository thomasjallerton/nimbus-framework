package com.nimbusframework.nimbuscore.annotations.function.repeatable;

import com.nimbusframework.nimbuscore.annotations.function.WebSocketServerlessFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebSocketServerlessFunctions {
    WebSocketServerlessFunction[] value();
}

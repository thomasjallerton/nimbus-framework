package com.nimbusframework.nimbuscore.annotations.function.repeatable;

import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpServerlessFunctions {
    HttpServerlessFunction[] value();
}

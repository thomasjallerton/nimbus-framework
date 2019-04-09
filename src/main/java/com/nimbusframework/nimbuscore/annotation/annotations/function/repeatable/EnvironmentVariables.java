package com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable;

import com.nimbusframework.nimbuscore.annotation.annotations.function.EnvironmentVariable;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnvironmentVariables {
    EnvironmentVariable[] value();
}


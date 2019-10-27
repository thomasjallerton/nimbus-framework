package com.nimbusframework.nimbuscore.annotations.function.repeatable;

import com.nimbusframework.nimbuscore.annotations.function.EnvironmentVariable;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnvironmentVariables {
    EnvironmentVariable[] value();
}


package com.nimbusframework.nimbuscore.annotations.function;


import com.nimbusframework.nimbuscore.annotations.function.repeatable.EnvironmentVariables;

import java.lang.annotation.*;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(EnvironmentVariables.class)
public @interface EnvironmentVariable {
    String key();
    String value();
    String testValue() default "NIMBUS_NOT_SET";
    String[] stages() default {};
}



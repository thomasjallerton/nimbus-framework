package com.nimbusframework.nimbuscore.annotation.annotations.function;


import com.nimbusframework.nimbuscore.annotation.annotations.NimbusConstants;
import com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.EnvironmentVariables;

import java.lang.annotation.*;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(EnvironmentVariables.class)
public @interface EnvironmentVariable {
    String key();
    String value();
    String testValue() default "NIMBUS_NOT_SET";
    String[] stages() default {NimbusConstants.stage};
}



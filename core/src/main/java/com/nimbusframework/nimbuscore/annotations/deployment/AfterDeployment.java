package com.nimbusframework.nimbuscore.annotations.deployment;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AfterDeployments.class)
public @interface AfterDeployment {
    boolean isTest() default false;
    String[] stages() default {};
}

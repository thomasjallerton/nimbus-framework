package com.nimbusframework.nimbuscore.annotation.annotations.deployment;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ForceDependencies.class)
public @interface ForceDependency {
    String[] classPaths();
}

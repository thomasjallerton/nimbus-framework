package com.nimbusframework.nimbusaws.annotation.annotations.cognito;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesCognitoUserPools.class)
public @interface UsesCognitoUserPool {
    Class<?> userPool();
    String[] stages() default {};
}

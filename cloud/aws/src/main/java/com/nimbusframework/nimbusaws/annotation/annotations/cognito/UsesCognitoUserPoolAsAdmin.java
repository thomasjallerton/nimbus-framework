package com.nimbusframework.nimbusaws.annotation.annotations.cognito;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesCognitoUserPoolAsAdmins.class)
public @interface UsesCognitoUserPoolAsAdmin {
    Class<?> userPool();
    String[] stages() default {};
}

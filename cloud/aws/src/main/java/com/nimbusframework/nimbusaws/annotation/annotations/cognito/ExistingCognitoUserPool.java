package com.nimbusframework.nimbusaws.annotation.annotations.cognito;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ExistingCognitoUserPools.class)
public @interface ExistingCognitoUserPool {
    String arn();

    /**
     * Optional, only needed if you need to add or remove users from groups
     */
    String userPoolId() default "";

    String[] stages() default {};
}

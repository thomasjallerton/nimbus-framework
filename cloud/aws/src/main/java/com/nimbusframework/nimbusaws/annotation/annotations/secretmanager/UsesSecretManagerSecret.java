package com.nimbusframework.nimbusaws.annotation.annotations.secretmanager;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesSecretManagerSecrets.class)
public @interface UsesSecretManagerSecret {
    String secretArn();
    String[] stages() default {};
}

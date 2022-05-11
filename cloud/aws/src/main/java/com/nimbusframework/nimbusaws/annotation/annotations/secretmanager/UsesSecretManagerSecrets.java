package com.nimbusframework.nimbusaws.annotation.annotations.secretmanager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UsesSecretManagerSecrets {
  UsesSecretManagerSecret[] value();
}

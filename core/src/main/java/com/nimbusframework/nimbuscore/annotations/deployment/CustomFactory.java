package com.nimbusframework.nimbuscore.annotations.deployment;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomFactory {
    Class<? extends CustomFactoryInterface> value();
}

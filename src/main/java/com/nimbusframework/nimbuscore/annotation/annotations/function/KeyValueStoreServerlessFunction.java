package com.nimbusframework.nimbuscore.annotation.annotations.function;

import com.nimbusframework.nimbuscore.annotation.annotations.NimbusConstants;
import com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.KeyValueStoreServerlessFunctions;
import com.nimbusframework.nimbuscore.annotation.annotations.persistent.StoreEventType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(KeyValueStoreServerlessFunctions.class)
public @interface KeyValueStoreServerlessFunction {
    Class<?> dataModel();
    StoreEventType method();
    int timeout() default 10;
    int memory() default 1024;
    String[] stages() default {NimbusConstants.stage};
}

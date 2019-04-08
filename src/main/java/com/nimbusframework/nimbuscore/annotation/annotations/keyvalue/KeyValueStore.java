package com.nimbusframework.nimbuscore.annotation.annotations.keyvalue;

import com.nimbusframework.nimbuscore.annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(KeyValueStores.class)
public @interface KeyValueStore {
    String tableName() default "";
    Class<?> keyType();
    String keyName() default "PrimaryKey";
    String existingArn() default "";
    String[] stages() default {NimbusConstants.stage};
}

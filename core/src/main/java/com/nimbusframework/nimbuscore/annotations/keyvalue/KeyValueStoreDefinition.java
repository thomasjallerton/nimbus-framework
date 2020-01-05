package com.nimbusframework.nimbuscore.annotations.keyvalue;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(KeyValueStoreDefinitions.class)
public @interface KeyValueStoreDefinition {
    String tableName() default "";
    Class<?> keyType();
    String keyName() default "PrimaryKey";
    String[] stages() default {NimbusConstants.stage};
}

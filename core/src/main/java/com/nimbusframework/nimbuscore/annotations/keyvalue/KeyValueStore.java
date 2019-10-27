package com.nimbusframework.nimbuscore.annotations.keyvalue;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(KeyValueStores.class)
public @interface KeyValueStore {
    String tableName() default "";
    Class<?> keyType();
    String keyName() default "PrimaryKey";
    String existingArn() default "";
    int readCapacityUnits() default 5;
    int writeCapacityUnits() default 5;
    String[] stages() default {NimbusConstants.stage};
}

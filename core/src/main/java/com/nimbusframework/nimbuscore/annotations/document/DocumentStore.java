package com.nimbusframework.nimbuscore.annotations.document;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DocumentStores.class)
public @interface DocumentStore {
    String tableName() default "";
    String existingArn() default "";
    int readCapacityUnits() default 5;
    int writeCapacityUnits() default 5;
    String[] stages() default {NimbusConstants.stage};
}

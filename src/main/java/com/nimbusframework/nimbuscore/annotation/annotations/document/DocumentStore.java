package com.nimbusframework.nimbuscore.annotation.annotations.document;

import com.nimbusframework.nimbuscore.annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DocumentStores.class)
public @interface DocumentStore {
    String tableName() default "";
    String existingArn() default "";
    String[] stages() default {NimbusConstants.stage};
}

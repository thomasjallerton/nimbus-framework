package com.nimbusframework.nimbuscore.annotations.document;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DocumentStoreDefinitions.class)
public @interface DocumentStoreDefinition {
    String tableName() default "";
    String[] stages() default {NimbusConstants.stage};
}

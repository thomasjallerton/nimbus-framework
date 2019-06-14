package com.nimbusframework.nimbuscore.annotation.annotations.file;

import com.nimbusframework.nimbuscore.annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesFileStorages.class)
public @interface UsesFileStorage {
    String bucketName();
    String[] stages() default {NimbusConstants.stage};
}

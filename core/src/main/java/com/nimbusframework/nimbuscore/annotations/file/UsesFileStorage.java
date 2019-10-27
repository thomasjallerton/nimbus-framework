package com.nimbusframework.nimbuscore.annotations.file;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesFileStorages.class)
public @interface UsesFileStorage {
    String bucketName();
    String[] stages() default {NimbusConstants.stage};
}

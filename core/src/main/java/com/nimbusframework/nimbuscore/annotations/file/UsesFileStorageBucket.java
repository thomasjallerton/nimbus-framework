package com.nimbusframework.nimbuscore.annotations.file;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesFileStorageBuckets.class)
public @interface UsesFileStorageBucket {
    Class<?> fileStorageBucket();
    String[] stages() default {NimbusConstants.stage};
}

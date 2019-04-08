package com.nimbusframework.nimbuscore.annotation.annotations.file;

import com.nimbusframework.nimbuscore.annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FileStorageBuckets.class)
public @interface FileStorageBucket {
    String bucketName();
    boolean staticWebsite() default false;
    String indexFile() default "index.html";
    String errorFile() default "error.html";
    String[] stages() default {NimbusConstants.stage};
}

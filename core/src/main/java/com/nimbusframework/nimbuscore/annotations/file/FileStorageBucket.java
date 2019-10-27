package com.nimbusframework.nimbuscore.annotations.file;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FileStorageBuckets.class)
public @interface FileStorageBucket {
    String bucketName();
    boolean staticWebsite() default false;
    String[] allowedCorsOrigins() default {};
    String indexFile() default "index.html";
    String errorFile() default "error.html";
    String[] stages() default {NimbusConstants.stage};
}

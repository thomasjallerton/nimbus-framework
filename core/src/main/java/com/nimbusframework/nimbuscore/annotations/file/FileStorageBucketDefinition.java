package com.nimbusframework.nimbuscore.annotations.file;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FileStorageBucketDefinitions.class)
public @interface FileStorageBucketDefinition {
    String bucketName();
    boolean staticWebsite() default false;
    String[] allowedCorsOrigins() default {};
    String indexFile() default "index.html";
    String errorFile() default "error.html";
    String[] stages() default {};
}

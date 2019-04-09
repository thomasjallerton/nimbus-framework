package com.nimbusframework.nimbuscore.annotation.annotations.deployment;

import com.nimbusframework.nimbuscore.annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FileUploads.class)
public @interface FileUpload {
    String bucketName();
    String localPath();
    String targetPath();
    boolean substituteNimbusVariables() default false;
    String[] stages() default {NimbusConstants.stage};
}

package com.nimbusframework.nimbuscore.annotations.deployment;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FileUploads.class)
public @interface FileUpload {
    Class<?> fileStorageBucket();
    String localPath();
    String targetPath();
    boolean substituteNimbusVariables() default false;
    String[] stages() default {};
}

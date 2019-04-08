package com.nimbusframework.nimbuscore.annotation.annotations.deployment;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FileUploads {
    FileUpload[] value();
}

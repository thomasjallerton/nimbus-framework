package com.nimbusframework.nimbuscore.annotation.annotations.deployment;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FileUploads {
    FileUpload[] value();
}

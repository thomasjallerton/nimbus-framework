package com.nimbusframework.nimbuscore.annotations.function;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;
import com.nimbusframework.nimbuscore.annotations.file.FileStorageEventType;
import com.nimbusframework.nimbuscore.annotations.function.repeatable.FileStorageServerlessFunctions;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FileStorageServerlessFunctions.class)
public @interface FileStorageServerlessFunction {
    Class<?> fileStorageBucket();
    FileStorageEventType eventType();
    int timeout() default 10;
    int memory() default 1024;
    String[] stages() default {NimbusConstants.stage};
}

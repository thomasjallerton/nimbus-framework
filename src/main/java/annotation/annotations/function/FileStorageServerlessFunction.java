package annotation.annotations.function;

import annotation.annotations.NimbusConstants;
import annotation.annotations.file.FileStorageEventType;
import annotation.annotations.function.repeatable.FileStorageServerlessFunctions;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FileStorageServerlessFunctions.class)
public @interface FileStorageServerlessFunction {
    String bucketName();
    FileStorageEventType eventType();
    int timeout() default 10;
    int memory() default 1024;
    String[] stages() default {NimbusConstants.stage};
}

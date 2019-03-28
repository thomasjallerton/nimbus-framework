package annotation.annotations.deployment;

import annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FileUploads.class)
public @interface FileUpload {
    String bucketName();
    String localPath();
    String targetPath();
    //boolean recursive() default false;
    String[] stages() default {NimbusConstants.stage};
}

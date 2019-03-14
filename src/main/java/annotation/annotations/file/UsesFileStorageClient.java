package annotation.annotations.file;

import annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesFileStorageClients.class)
public @interface UsesFileStorageClient {
    String bucketName();
    String[] stages() default {NimbusConstants.stage};
}

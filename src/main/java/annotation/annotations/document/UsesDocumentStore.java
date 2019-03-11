package annotation.annotations.document;

import annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesDocumentStores.class)
public @interface UsesDocumentStore {
    Class<?> dataModel();
    String[] stages() default {NimbusConstants.stage};
}

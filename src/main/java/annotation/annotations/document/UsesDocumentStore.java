package annotation.annotations.document;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesDocumentStores.class)
public @interface UsesDocumentStore {
    Class<?> dataModel();
    String stage() default "dev";
}

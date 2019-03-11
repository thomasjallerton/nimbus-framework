package annotation.annotations.keyvalue;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesKeyValueStores.class)
public @interface UsesKeyValueStore {
    Class<?> dataModel();
    String stage() default "dev";
}

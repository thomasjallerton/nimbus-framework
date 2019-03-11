package annotation.annotations.keyvalue;

import annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesKeyValueStores.class)
public @interface UsesKeyValueStore {
    Class<?> dataModel();
    String[] stages() default {NimbusConstants.stage};
}

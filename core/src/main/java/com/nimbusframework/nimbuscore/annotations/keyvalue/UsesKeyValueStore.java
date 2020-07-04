package com.nimbusframework.nimbuscore.annotations.keyvalue;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesKeyValueStores.class)
public @interface UsesKeyValueStore {
    Class<?> dataModel();
    String[] stages() default {};
}

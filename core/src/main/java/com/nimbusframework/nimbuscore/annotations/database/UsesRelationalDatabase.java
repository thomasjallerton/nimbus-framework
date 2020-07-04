package com.nimbusframework.nimbuscore.annotations.database;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesRelationalDatabases.class)
public @interface UsesRelationalDatabase {
    Class<?> dataModel();
    String[] stages() default {};
}

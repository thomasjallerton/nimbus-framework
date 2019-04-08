package com.nimbusframework.nimbuscore.annotation.annotations.database;

import com.nimbusframework.nimbuscore.annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RelationalDatabases.class)
public @interface RelationalDatabase {
    String name();
    String username();
    String password();
    DatabaseSize databaseSize() default DatabaseSize.FREE;
    DatabaseLanguage databaseLanguage();
    int allocatedSizeGB() default 20;
    String[] stages() default {NimbusConstants.stage};
}

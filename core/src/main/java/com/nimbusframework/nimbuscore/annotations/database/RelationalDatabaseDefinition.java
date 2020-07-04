package com.nimbusframework.nimbuscore.annotations.database;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RelationalDatabaseDefinitions.class)
public @interface RelationalDatabaseDefinition {
    String name();
    String username();
    String password();
    DatabaseSize databaseClass() default DatabaseSize.FREE;
    DatabaseLanguage databaseLanguage();
    int allocatedSizeGB() default 20;
    String[] stages() default {};
}

package com.nimbusframework.nimbusaws.annotation.annotations.database;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RdsDatabases.class)
public @interface RdsDatabase {
  String name();
  String username();
  String password();
  String awsDatabaseInstance();
  DatabaseLanguage databaseLanguage();
  int allocatedSizeGB() default 20;
  String[] stages() default {NimbusConstants.stage};
}

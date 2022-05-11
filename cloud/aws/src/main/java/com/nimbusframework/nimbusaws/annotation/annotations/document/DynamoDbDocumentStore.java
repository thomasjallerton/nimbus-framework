package com.nimbusframework.nimbusaws.annotation.annotations.document;


import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DynamoDbDocumentStores.class)
public @interface DynamoDbDocumentStore {
  String tableName() default "";
  int readCapacityUnits() default 5;
  int writeCapacityUnits() default 5;
  String[] stages() default {};
}

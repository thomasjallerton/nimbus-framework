package com.nimbusframework.nimbusaws.annotation.annotations.document;


import com.nimbusframework.nimbuscore.annotations.NimbusConstants;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DynamoDbDocumentStores.class)
public @interface DynamoDbDocumentStore {
  String tableName() default "";
  String existingArn() default "";
  int readCapacityUnits() default 5;
  int writeCapacityUnits() default 5;
  String[] stages() default {NimbusConstants.stage};
}

package com.nimbusframework.nimbusaws.annotation.annotations.keyvalue;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DynamoDbKeyValueStores.class)
public @interface DynamoDbKeyValueStore {
  String tableName() default "";
  Class<?> keyType();
  String keyName() default "PrimaryKey";
  String existingArn() default "";
  int readCapacityUnits() default 5;
  int writeCapacityUnits() default 5;
  String[] stages() default {};
}

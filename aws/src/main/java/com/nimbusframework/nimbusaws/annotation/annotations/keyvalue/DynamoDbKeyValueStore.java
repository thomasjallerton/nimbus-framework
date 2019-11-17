package com.nimbusframework.nimbusaws.annotation.annotations.keyvalue;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
  String[] stages() default {NimbusConstants.stage};
}

package com.nimbusframework.nimbusaws.annotation.annotations.nativeimage;

import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStores;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface RegisterForReflection {}

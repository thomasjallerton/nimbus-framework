package com.nimbusframework.nimbusaws.examples;

import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStore;

@DynamoDbKeyValueStore(keyType = Integer.class)
public class KeyValueStoreNoTableName {

  private String value;

  public KeyValueStoreNoTableName(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

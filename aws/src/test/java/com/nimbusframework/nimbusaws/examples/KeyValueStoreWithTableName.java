package com.nimbusframework.nimbusaws.examples;

import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStore;

@DynamoDbKeyValueStore(keyType = Integer.class, tableName = "test", keyName = "test")
public class KeyValueStoreWithTableName {

  private String value;

  public KeyValueStoreWithTableName(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

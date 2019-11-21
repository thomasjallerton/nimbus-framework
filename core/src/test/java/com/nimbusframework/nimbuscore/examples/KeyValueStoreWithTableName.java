package com.nimbusframework.nimbuscore.examples;

import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStore;

@KeyValueStore(keyType = Integer.class, tableName = "test", keyName = "test")
public class KeyValueStoreWithTableName {

  private String value;

  public KeyValueStoreWithTableName(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

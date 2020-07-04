package com.nimbusframework.nimbuscore.examples.keyvalue;

import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition;

@KeyValueStoreDefinition(keyType = Integer.class, tableName = "test", keyName = "test")
public class KeyValueStoreWithTableName {

  private String value;

  public KeyValueStoreWithTableName(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

package com.nimbusframework.nimbuscore.examples.keyvalue;

import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition;

@KeyValueStoreDefinition(keyType = Integer.class, stages = "dev")
public class KeyValueStoreNoTableName {

  private String value;

  public KeyValueStoreNoTableName(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

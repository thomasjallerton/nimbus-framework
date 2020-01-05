package com.nimbusframework.nimbuscore.examples;

import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition;

@KeyValueStoreDefinition(keyType = Integer.class)
public class KeyValueStoreNoTableName {

  private String value;

  public KeyValueStoreNoTableName(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

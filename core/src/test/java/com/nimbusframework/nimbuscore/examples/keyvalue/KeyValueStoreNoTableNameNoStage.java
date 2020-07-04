package com.nimbusframework.nimbuscore.examples.keyvalue;

import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStoreDefinition;

@KeyValueStoreDefinition(keyType = Integer.class)
public class KeyValueStoreNoTableNameNoStage {

  private String value;

  public KeyValueStoreNoTableNameNoStage(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

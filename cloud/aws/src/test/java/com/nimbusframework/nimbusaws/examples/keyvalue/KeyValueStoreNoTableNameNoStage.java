package com.nimbusframework.nimbusaws.examples.keyvalue;

import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStore;
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute;

import java.util.Objects;

@DynamoDbKeyValueStore(keyType = Integer.class)
public class KeyValueStoreNoTableNameNoStage {

  @Attribute
  private String value;

  public KeyValueStoreNoTableNameNoStage() {}

  public KeyValueStoreNoTableNameNoStage(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeyValueStoreNoTableNameNoStage that = (KeyValueStoreNoTableNameNoStage) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

}

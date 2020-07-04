package com.nimbusframework.nimbusaws.examples.document;

import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore;
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute;
import com.nimbusframework.nimbuscore.annotations.persistent.Key;
import java.util.Objects;

@DynamoDbDocumentStore(stages = {"dev"})
public class DocumentStoreNoTableName {

  @Key
  private String string;

  @Attribute
  private Integer integer;

  public DocumentStoreNoTableName() {}

  public DocumentStoreNoTableName(String string, Integer integer) {
    this.string = string;
    this.integer = integer;
  }

  public String getString() {
    return string;
  }

  public Integer getInteger() {
    return integer;
  }

  public void setString(String string) {
    this.string = string;
  }

  public void setInteger(Integer integer) {
    this.integer = integer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DocumentStoreNoTableName that = (DocumentStoreNoTableName) o;
    return Objects.equals(string, that.string) &&
        Objects.equals(integer, that.integer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(string, integer);
  }
}

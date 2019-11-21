package com.nimbusframework.nimbusaws.examples;

import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore;
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute;
import com.nimbusframework.nimbuscore.annotations.persistent.Key;

@DynamoDbDocumentStore
public class DocumentStoreNoTableName {

  @Key
  private String string;

  @Attribute
  private Integer integer;

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

}

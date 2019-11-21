package com.nimbusframework.nimbusaws.examples;

import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore;
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute;
import com.nimbusframework.nimbuscore.annotations.persistent.Key;

@DynamoDbDocumentStore(tableName = "test")
public class DocumentStoreWithTableName {

  @Key
  private String string;

  @Attribute
  private Integer integer;

  public DocumentStoreWithTableName(String string, Integer integer) {
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

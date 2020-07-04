package com.nimbusframework.nimbuscore.examples.document;

import com.nimbusframework.nimbuscore.annotations.document.DocumentStoreDefinition;
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute;
import com.nimbusframework.nimbuscore.annotations.persistent.Key;

@DocumentStoreDefinition(tableName = "test")
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

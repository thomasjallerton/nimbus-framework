package com.nimbusframework.nimbusaws.examples;

public class SimpleObject {

  private String value;

  public SimpleObject(String value) { this.value = value; }
  public SimpleObject() {}

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}

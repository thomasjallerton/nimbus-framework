package com.nimbusframework.nimbusaws.examples;

import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction;

public class BasicFunctionHandler {

  @BasicServerlessFunction
  public String exampleFunc() {
    return "HELLO";
  }
}

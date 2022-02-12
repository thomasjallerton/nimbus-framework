package com.nimbusframework.nimbusaws.wrappers.customRuntimeEntry;


@FunctionalInterface
public interface LambdaExecutionFunction {

    void accept(InvocationResponse invocationResponse, CustomContext customContext) throws Exception;

}

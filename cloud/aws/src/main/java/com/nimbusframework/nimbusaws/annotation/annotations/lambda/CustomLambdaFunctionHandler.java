package com.nimbusframework.nimbusaws.annotation.annotations.lambda;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define a custom lambda function that will be uploaded instead of the generated java lambda.
 * Permissions will be attached to the function as normal
 * This function will not be used locally. You can use the ClientBuilder.isLocal() method to validate this.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface CustomLambdaFunctionHandler {
    String file();
    String handler();
    String runtime();
}

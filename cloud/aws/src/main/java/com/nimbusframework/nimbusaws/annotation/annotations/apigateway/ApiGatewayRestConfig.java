package com.nimbusframework.nimbusaws.annotation.annotations.apigateway;

import java.lang.annotation.*;

/**
 * Optional config to configure AWS specific parts of an API gateway REST deployment.
 * Must be unique across the deployment stage
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ApiGatewayRestConfigs.class)
public @interface ApiGatewayRestConfig {
    /**
     * Optional. Must be a class annotated with @ExistingCognitoUserPool, or a class that implements ApiGatewayLambdaAuthorizer
     */
    Class<?> authorizer() default void.class;

    String authorizationHeader() default "Authorization";

    int authorizationCacheTtl() default 300;

    /**
     * The stages this config will be deployed to
     */
    String[] stages() default {};
}

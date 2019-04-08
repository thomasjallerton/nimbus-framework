package com.nimbusframework.nimbuscore.annotation.annotations.websocket;

import com.nimbusframework.nimbuscore.annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesServerlessFunctionWebSocketClients.class)
public @interface UsesServerlessFunctionWebSocketClient {
    String[] stages() default {NimbusConstants.stage};
}

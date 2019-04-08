package com.nimbusframework.nimbuscore.annotation.annotations.websocket;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UsesServerlessFunctionWebSocketClients {
    UsesServerlessFunctionWebSocketClient[] value();
}

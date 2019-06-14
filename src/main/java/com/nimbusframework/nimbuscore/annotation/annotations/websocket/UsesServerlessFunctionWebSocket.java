package com.nimbusframework.nimbuscore.annotation.annotations.websocket;

import com.nimbusframework.nimbuscore.annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesServerlessFunctionWebSockets.class)
public @interface UsesServerlessFunctionWebSocket {
    String[] stages() default {NimbusConstants.stage};
}

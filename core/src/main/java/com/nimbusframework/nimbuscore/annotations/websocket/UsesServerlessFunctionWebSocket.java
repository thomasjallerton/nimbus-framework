package com.nimbusframework.nimbuscore.annotations.websocket;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesServerlessFunctionWebSockets.class)
public @interface UsesServerlessFunctionWebSocket {
    String[] stages() default {NimbusConstants.stage};
}

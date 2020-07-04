package com.nimbusframework.nimbuscore.annotations.websocket;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesServerlessFunctionWebSockets.class)
public @interface UsesServerlessFunctionWebSocket {
    String[] stages() default {};
}

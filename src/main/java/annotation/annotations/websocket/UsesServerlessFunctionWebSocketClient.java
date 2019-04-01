package annotation.annotations.websocket;

import annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesServerlessFunctionWebSocketClients.class)
public @interface UsesServerlessFunctionWebSocketClient {
    String[] stages() default {NimbusConstants.stage};
}

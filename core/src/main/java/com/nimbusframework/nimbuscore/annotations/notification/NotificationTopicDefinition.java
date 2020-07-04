package com.nimbusframework.nimbuscore.annotations.notification;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(NotificationTopicDefinitions.class)
public @interface NotificationTopicDefinition {
    String topicName();
    String[] stages() default {};
}

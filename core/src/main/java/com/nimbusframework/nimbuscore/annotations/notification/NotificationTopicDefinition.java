package com.nimbusframework.nimbuscore.annotations.notification;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(NotificationTopicDefinitions.class)
public @interface NotificationTopicDefinition {
    String topicName();
    String[] stages() default {NimbusConstants.stage};
}

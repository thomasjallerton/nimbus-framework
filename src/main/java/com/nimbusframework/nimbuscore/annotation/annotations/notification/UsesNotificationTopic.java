package com.nimbusframework.nimbuscore.annotation.annotations.notification;

import com.nimbusframework.nimbuscore.annotation.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesNotificationTopics.class)
public @interface UsesNotificationTopic {
    String topic();
    String[] stages() default {NimbusConstants.stage};
}

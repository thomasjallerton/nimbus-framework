package com.nimbusframework.nimbuscore.annotations.notification;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesNotificationTopics.class)
public @interface UsesNotificationTopic {
    Class<?> notificationTopic();
    String[] stages() default {NimbusConstants.stage};
}

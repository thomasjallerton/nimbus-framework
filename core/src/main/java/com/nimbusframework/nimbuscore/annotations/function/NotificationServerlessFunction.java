package com.nimbusframework.nimbuscore.annotations.function;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;
import com.nimbusframework.nimbuscore.annotations.function.repeatable.NotificationServerlessFunctions;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(NotificationServerlessFunctions.class)
public @interface NotificationServerlessFunction {
    Class<?> notificationTopic();
    int timeout() default 10;
    int memory() default 1024;
    String[] stages() default {NimbusConstants.stage};
}

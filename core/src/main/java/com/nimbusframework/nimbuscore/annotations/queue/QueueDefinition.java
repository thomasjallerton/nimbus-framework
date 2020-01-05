package com.nimbusframework.nimbuscore.annotations.queue;

import com.nimbusframework.nimbuscore.annotations.NimbusConstants;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(QueueDefinitions.class)
public @interface QueueDefinition {
    String queueId();
    String[] stages() default {NimbusConstants.stage};
}

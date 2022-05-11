package com.nimbusframework.nimbuscore.annotations.queue;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(QueueDefinitions.class)
public @interface QueueDefinition {
    /**
     * Unique (to your cloud account) identifier for a queue. In the cloud account this will be combined
     * with the stage to create the full identifier.
     */
    String queueId();

    /**
     * The amount of time before an item that is being processed by a queue consumer is returned
     * to the queue to be reprocessed. The unit is seconds.
     */
    int itemProcessingTimeout() default 30;

    /**
     * The stages that the queue will be deployed to.
     */
    String[] stages() default {};
}

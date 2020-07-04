package com.nimbusframework.nimbuscore.annotations.queue;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesQueues.class)
public @interface UsesQueue {
    Class<?> queue();
    String[] stages() default {};
}

package annotation.annotations.queue;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesQueues.class)
public @interface UsesQueue {
    String id();
    String stage() default "dev";
}

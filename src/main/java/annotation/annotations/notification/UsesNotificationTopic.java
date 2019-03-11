package annotation.annotations.notification;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UsesNotificationTopics.class)
public @interface UsesNotificationTopic {
    String topic();
    String stage() default "dev";
}

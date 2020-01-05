package handlers;

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.annotations.notification.UsesNotificationTopic;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;
import models.NotificationTopic;

public class UsesNotificationTopicHandler {

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    @UsesNotificationTopic(notificationTopic = NotificationTopic.class)
    public void func() {
        ClientBuilder.getNotificationClient("Test");
    }

}

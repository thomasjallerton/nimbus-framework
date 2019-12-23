package handlers;

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.annotations.notification.UsesNotificationTopic;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;

public class UsesNotificationTopicHandler {

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    @UsesNotificationTopic(topic = "Test")
    public void func() {
        ClientBuilder.getNotificationClient("Test");
    }

}

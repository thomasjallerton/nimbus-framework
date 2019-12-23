package handlers;

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.annotations.notification.UsesNotificationTopic;
import com.nimbusframework.nimbuscore.annotations.queue.UsesQueue;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;

public class UsesQueueHandler {

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    @UsesQueue(id = "messageQueue")
    public void func() {
        ClientBuilder.getQueueClient("messageQueue");
    }

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test2")
    @UsesQueue(id = "nonexistent")
    public void func2() {
        ClientBuilder.getQueueClient("messageQueue");
    }
}

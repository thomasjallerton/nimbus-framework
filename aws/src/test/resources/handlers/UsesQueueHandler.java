package handlers;

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.annotations.notification.UsesNotificationTopic;
import com.nimbusframework.nimbuscore.annotations.queue.UsesQueue;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;
import models.Queue;

public class UsesQueueHandler {

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    @UsesQueue(queue = Queue.class)
    public void func() {
        ClientBuilder.getQueueClient("messageQueue");
    }

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test2")
    @UsesQueue(queue = Queue.class)
    public void func2() {
        ClientBuilder.getQueueClient(Queue.class);
    }
}

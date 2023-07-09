package handlers.basic;

import com.nimbusframework.nimbusaws.annotation.annotations.lambda.CustomLambdaFunctionHandler;
import com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction;
import com.nimbusframework.nimbuscore.annotations.notification.UsesNotificationTopic;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;
import models.NotificationTopic;

public class BasicCustomFunctionHandler {

    @BasicServerlessFunction
    @UsesNotificationTopic(notificationTopic = NotificationTopic.class)
    @CustomLambdaFunctionHandler(file = "testfile", handler = "handler", runtime = "provided")
    public void func() {
        ClientBuilder.getNotificationClient(NotificationTopic.class);
    }

}

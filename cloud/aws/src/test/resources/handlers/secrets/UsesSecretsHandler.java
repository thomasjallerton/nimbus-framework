package handlers.secrets;

import com.nimbusframework.nimbuscore.annotations.http.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbusaws.annotation.annotations.secretmanager.UsesSecretManagerSecret;
import com.nimbusframework.nimbusaws.clients.AwsClientBuilder;
import models.cognito.UserPool;

public class UsesSecretsHandler {

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    @UsesSecretManagerSecret(secretArn = "arn:partition:service:region:account-id:resource-id")
    public void func() {
        AwsClientBuilder.getSecretManagerClient();
    }


}

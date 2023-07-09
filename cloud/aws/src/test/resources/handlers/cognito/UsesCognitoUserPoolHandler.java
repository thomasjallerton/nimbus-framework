package handlers.cognito;

import com.nimbusframework.nimbuscore.annotations.http.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbusaws.annotation.annotations.cognito.UsesCognitoUserPool;
import com.nimbusframework.nimbusaws.annotation.annotations.cognito.UsesCognitoUserPoolAsAdmin;
import com.nimbusframework.nimbusaws.clients.AwsClientBuilder;
import models.cognito.UserPool;

public class UsesCognitoUserPoolHandler {

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    @UsesCognitoUserPool(userPool = UserPool.class)
    public void func() {
        AwsClientBuilder.getCognitoClient(UserPool.class);
    }

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    @UsesCognitoUserPoolAsAdmin(userPool = UserPool.class)
    public void func2() {
        AwsClientBuilder.getCognitoClient(UserPool.class);
    }

}

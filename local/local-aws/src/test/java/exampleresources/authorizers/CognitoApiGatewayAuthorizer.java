package exampleresources.authorizers;

import com.nimbusframework.nimbusaws.annotation.annotations.apigateway.ApiGatewayRestConfig;
import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import exampleresources.UserPool;

@ApiGatewayRestConfig(
        authorizer = UserPool.class
)
public class CognitoApiGatewayAuthorizer {

    @HttpServerlessFunction(method = HttpMethod.GET, path = "hello/world")
    public String get() {
        return "helloworld";
    }

}

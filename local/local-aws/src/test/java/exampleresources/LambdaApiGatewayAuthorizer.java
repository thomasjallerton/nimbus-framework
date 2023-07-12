package exampleresources;

import com.nimbusframework.nimbusaws.annotation.annotations.apigateway.ApiGatewayRestConfig;
import com.nimbusframework.nimbuscore.annotations.http.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import exampleresources.authorizers.ExampleAuthorizerPathStar;

@ApiGatewayRestConfig(
        authorizer = ExampleAuthorizerPathStar.class
)
public class LambdaApiGatewayAuthorizer {

    @HttpServerlessFunction(method = HttpMethod.GET, path = "hello/world")
    public String get() {
        return "helloworld";
    }

    @HttpServerlessFunction(method = HttpMethod.PUT, path = "hello/world")
    public String put() {
        return "helloworld";
    }

}

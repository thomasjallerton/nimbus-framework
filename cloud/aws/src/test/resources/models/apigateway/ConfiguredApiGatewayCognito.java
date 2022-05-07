package models.apigateway;

import com.nimbusframework.nimbusaws.annotation.annotations.apigateway.ApiGatewayRestConfig;
import model.cognito.UserPool;

@ApiGatewayRestConfig(
        authorizer = UserPool.class
)
public class ConfiguredApiGatewayCustomHandler {

}

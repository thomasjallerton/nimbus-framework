package models.apigateway;

import com.nimbusframework.nimbusaws.annotation.annotations.apigateway.ApiGatewayRestConfig;
import models.cognito.UserPool;

@ApiGatewayRestConfig(
        authorizer = UserPool.class
)
public class ConfiguredApiGatewayCognito {

}

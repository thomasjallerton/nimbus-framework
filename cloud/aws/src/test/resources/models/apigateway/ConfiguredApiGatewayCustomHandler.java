package models.apigateway;

import com.nimbusframework.nimbusaws.annotation.annotations.apigateway.ApiGatewayRestConfig;
import handlers.apigateway.CustomAuthorizer;

@ApiGatewayRestConfig(
        authorizer = CustomAuthorizer.class,
        authorizationHeader = "Bearer",
        authorizationCacheTtl = 100
)
public class ConfiguredApiGatewayCustomHandler {

}

package models.apigateway;

import com.nimbusframework.nimbusaws.annotation.annotations.apigateway.ApiGatewayRestConfig;
import handlers.apigateway.Authorizer;

@ApiGatewayRestConfig(
        authorizer = Authorizer.class
)
public class ConfiguredApiGatewayLambdaHandler {

}

package handlers.apigateway;

import com.nimbusframework.nimbusaws.interfaces.ApiGatewayLambdaAuthorizer;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import java.lang.Override;

public class Authorizer implements ApiGatewayLambdaAuthorizer {

  @Override
  public IamPolicyResponse handleRequest(APIGatewayCustomAuthorizerEvent input, Context context) {
    return null;
  }

}

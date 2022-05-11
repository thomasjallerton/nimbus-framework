package com.nimbusframework.nimbusaws.interfaces;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;

public interface ApiGatewayLambdaAuthorizer extends RequestHandler<APIGatewayCustomAuthorizerEvent, IamPolicyResponse> {


}

package exampleresources.authorizers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.nimbusframework.nimbusaws.interfaces.ApiGatewayLambdaAuthorizer;

import java.util.List;
import java.util.Map;

import static com.amazonaws.services.lambda.runtime.events.IamPolicyResponse.VERSION_2012_10_17;

public class ExampleAuthorizerPathStar implements ApiGatewayLambdaAuthorizer {

    @Override
    public IamPolicyResponse handleRequest(APIGatewayCustomAuthorizerEvent input, Context context) {
        String[] methodArn = input.getMethodArn().split(":");

        String[] path = methodArn[5].split("/");
        String resource = "arn:aws:execute-api:" + methodArn[3] + ":" + methodArn[4] + ":" + path[0] + "/" + path[1] + "/GET/hello/*";

        return IamPolicyResponse.builder()
                .withPolicyDocument(
                        IamPolicyResponse.PolicyDocument.builder()
                                .withVersion(VERSION_2012_10_17)
                                .withStatement(List.of(IamPolicyResponse.allowStatement(resource)))
                                .build())
                .withContext(Map.of("key", "value"))
            .build();
    }

}

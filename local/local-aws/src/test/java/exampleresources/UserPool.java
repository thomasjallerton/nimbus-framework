package exampleresources;

import com.nimbusframework.nimbusaws.annotation.annotations.cognito.ExistingCognitoUserPool;

@ExistingCognitoUserPool(arn = "ard", userPoolId = "userPoolId")
public class UserPool {

}

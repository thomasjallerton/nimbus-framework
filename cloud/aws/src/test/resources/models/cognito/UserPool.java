package models.cognito;

import com.nimbusframework.nimbusaws.annotation.annotations.cognito.ExistingCognitoUserPool;

@ExistingCognitoUserPool(arn = "arn:partition:service:region:account-id:resource-id")
public class UserPool {}

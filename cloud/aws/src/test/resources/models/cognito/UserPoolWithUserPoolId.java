package models;

import com.nimbusframework.nimbusaws.annotation.annotations.cognito.ExistingCognitoUserPool;

@ExistingCognitoUserPool(arn = "arn:partition:service:region:account-id:resource-id", userPoolId = "userpoolid")
public class UserPoolWithUserPoolId {}

package com.nimbusframework.nimbusaws.examples.cognito;

import com.nimbusframework.nimbusaws.annotation.annotations.cognito.ExistingCognitoUserPool;

@ExistingCognitoUserPool(arn = "arn", userPoolId = "userPool", stages = { "stage1" })
@ExistingCognitoUserPool(arn = "arn", userPoolId = "userPool", stages = { "stage2" })
public class MultipleStagesCognito {

}

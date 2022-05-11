package com.nimbusframework.nimbusaws.cloudformation.generation.resources.cognito

import com.nimbusframework.nimbusaws.annotation.annotations.cognito.UsesCognitoUserPool
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.DataModelAnnotation

class UsesCognitoUserPoolAnnotation(private val usesCognitoUserPool: UsesCognitoUserPool): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return usesCognitoUserPool.userPool.java
    }

}

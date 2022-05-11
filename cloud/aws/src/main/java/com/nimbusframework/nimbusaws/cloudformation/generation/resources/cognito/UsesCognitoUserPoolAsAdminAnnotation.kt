package com.nimbusframework.nimbusaws.cloudformation.generation.resources.cognito

import com.nimbusframework.nimbusaws.annotation.annotations.cognito.UsesCognitoUserPoolAsAdmin
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.DataModelAnnotation

class UsesCognitoUserPoolAsAdminAnnotation(private val usesCognitoUserPoolAsAdmin: UsesCognitoUserPoolAsAdmin): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return usesCognitoUserPoolAsAdmin.userPool.java
    }

}

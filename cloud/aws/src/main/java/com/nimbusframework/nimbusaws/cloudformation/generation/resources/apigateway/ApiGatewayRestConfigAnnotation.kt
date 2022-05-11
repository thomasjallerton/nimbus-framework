package com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway

import com.nimbusframework.nimbusaws.annotation.annotations.apigateway.ApiGatewayRestConfig
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.DataModelAnnotation

class ApiGatewayRestConfigAnnotation(private val apiGatewayRestConfig: ApiGatewayRestConfig): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return apiGatewayRestConfig.authorizer.java
    }
}

package com.nimbusframework.nimbusaws.annotation.services.resources.annotations

import com.nimbusframework.nimbusaws.annotation.annotations.apigateway.ApiGatewayRestConfig
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.DataModelAnnotation

class ApiGatewayRestConfigAnnotation(private val apiGatewayRestConfig: ApiGatewayRestConfig): DataModelAnnotation() {

    override fun internalDataModel(): Class<out Any> {
        return apiGatewayRestConfig.authorizer.java
    }
}

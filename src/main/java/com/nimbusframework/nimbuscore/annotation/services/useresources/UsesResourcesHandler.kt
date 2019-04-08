package com.nimbusframework.nimbuscore.annotation.services.useresources

import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import javax.lang.model.element.Element

interface UsesResourcesHandler {

    fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource)

}
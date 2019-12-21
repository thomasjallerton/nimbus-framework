package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import javax.lang.model.element.Element

interface UsesResourcesProcessor {

    fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource)

}
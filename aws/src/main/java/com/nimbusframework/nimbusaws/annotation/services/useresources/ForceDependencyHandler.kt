package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbuscore.annotations.deployment.ForceDependency
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import javax.lang.model.element.Element

class ForceDependencyHandler: UsesResourcesHandler {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        for (environmentVariable in serverlessMethod.getAnnotationsByType(ForceDependency::class.java)) {
            environmentVariable.classPaths.forEach {
                functionResource.addExtraDependency(it)
            }
        }
    }
}
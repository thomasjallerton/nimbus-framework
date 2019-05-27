package com.nimbusframework.nimbuscore.annotation.services.useresources

import com.nimbusframework.nimbuscore.annotation.annotations.deployment.ForceDependency
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
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
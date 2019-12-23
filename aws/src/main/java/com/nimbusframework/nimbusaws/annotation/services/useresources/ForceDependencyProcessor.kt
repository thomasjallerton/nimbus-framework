package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbuscore.annotations.deployment.ForceDependency
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import javax.lang.model.element.Element

class ForceDependencyProcessor: UsesResourcesProcessor {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        for (forceDependency in serverlessMethod.getAnnotationsByType(ForceDependency::class.java)) {
            forceDependency.classPaths.forEach {
                functionResource.addExtraDependency(it)
            }
        }
    }
}
package com.nimbusframework.nimbusaws.cloudformation.generation.abstractions

import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.lang.model.element.Element

abstract class UsesResourcesProcessor(
        protected val nimbusState: NimbusState
) {

    protected val stageService = StageService(nimbusState.defaultStages)

    abstract fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource)

}

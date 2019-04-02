package annotation.services.useresources

import cloudformation.resource.function.FunctionResource
import javax.lang.model.element.Element

interface UsesResourcesHandler {

    fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource)

}
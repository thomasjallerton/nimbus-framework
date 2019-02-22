package annotation.processor

import annotation.models.resource.function.FunctionResource
import javax.lang.model.element.Element

data class FunctionInformation(var element: Element, var resource: FunctionResource)
package com.nimbusframework.nimbusaws.annotation.processor

import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import javax.lang.model.element.Element

data class FunctionInformation(
    var element: Element,
    var resource: FunctionResource,
    val canBeKeptWarm: Boolean = true
)
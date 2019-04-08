package com.nimbusframework.nimbuscore.annotation.processor

import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import javax.lang.model.element.Element

data class FunctionInformation(var element: Element, var resource: FunctionResource)
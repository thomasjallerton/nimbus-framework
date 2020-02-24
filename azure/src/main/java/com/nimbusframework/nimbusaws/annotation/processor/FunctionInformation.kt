package com.nimbusframework.nimbusaws.annotation.processor

import com.nimbusframework.nimbusaws.arm.resources.Resource
import javax.lang.model.element.Element

data class FunctionInformation(var element: Element, var resource: Resource)
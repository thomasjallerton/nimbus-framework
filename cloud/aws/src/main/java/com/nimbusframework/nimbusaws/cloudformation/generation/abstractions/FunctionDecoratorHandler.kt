package com.nimbusframework.nimbusaws.cloudformation.generation.abstractions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import javax.lang.model.element.Element

interface FunctionDecoratorHandler {

    fun handleDecorator(type: Element, functionInformation: List<FunctionInformation>)

}

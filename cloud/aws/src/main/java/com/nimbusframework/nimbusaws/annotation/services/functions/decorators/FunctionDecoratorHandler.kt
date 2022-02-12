package com.nimbusframework.nimbusaws.annotation.services.functions.decorators

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import javax.lang.model.element.Element

interface FunctionDecoratorHandler {

    fun handleDecorator(type: Element, functionInformation: List<FunctionInformation>)

}
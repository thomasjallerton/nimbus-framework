package com.nimbusframework.nimbusaws.cloudformation.generation

import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbusaws.lambda.handlers.HandlerProvider
import io.kotest.matchers.collections.shouldContain
import javax.lang.model.element.Element

object FunctionAssertions {

    fun functionShouldBeSetUpCorrectly(functionResource: FunctionResource, functionElement: Element, processingData: ProcessingData) {
        // handler information set up
        processingData.nimbusState.handlerFiles shouldContain FunctionResourceCreator.createHandlerInformation(functionElement, HandlerProvider(
            functionResource.getIdentifier().className,
            functionResource.getIdentifier().className + "::" + functionResource.getIdentifier().methodName
        ))

    }

}

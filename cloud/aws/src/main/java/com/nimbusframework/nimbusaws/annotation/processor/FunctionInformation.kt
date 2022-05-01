package com.nimbusframework.nimbusaws.annotation.processor

import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import javax.lang.model.element.Element

data class FunctionInformation(
    var element: Element,
    var resource: FunctionResource,
    /**
     * Specify the location of a method for use in a custom lambda runtime (like GraalVM)
     * Null if the user has specified a custom file and handler for the function
     */
    val awsMethodInformation: AwsMethodInformation? = null,
    val canBeKeptWarm: Boolean = true
)

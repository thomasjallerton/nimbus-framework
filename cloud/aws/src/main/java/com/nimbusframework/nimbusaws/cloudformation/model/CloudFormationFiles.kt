package com.nimbusframework.nimbusaws.cloudformation.model

import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

data class CloudFormationFiles(
    val createTemplate: CloudFormationTemplate,
    val updateTemplate: CloudFormationTemplate
) {

    private val additionalAttributes: MutableMap<String, String> = mutableMapOf()

    constructor(nimbusState: NimbusState, stage: String): this (
            CloudFormationTemplate(nimbusState, stage),
            CloudFormationTemplate(nimbusState, stage)
    )

    fun addAdditionalAttribute(type: Element, data: String) {
        additionalAttributes[type.simpleName.toString()] = data
    }

    fun getAdditionalAttribute(typeElement: TypeElement): String? {
        return additionalAttributes[typeElement.simpleName.toString()]
    }
}

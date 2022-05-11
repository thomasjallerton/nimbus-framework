package com.nimbusframework.nimbusaws.cloudformation.model

import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

data class CloudFormationFiles(
    val createTemplate: CloudFormationTemplate,
    val updateTemplate: CloudFormationTemplate
) {

    constructor(nimbusState: NimbusState, stage: String): this (
            CloudFormationTemplate(nimbusState, stage),
            CloudFormationTemplate(nimbusState, stage)
    )

}

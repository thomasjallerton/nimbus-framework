package com.nimbusframework.nimbuscore.cloudformation

import com.nimbusframework.nimbuscore.persisted.NimbusState

data class CloudFormationFiles(val createTemplate: CloudFormationTemplate, val updateTemplate: CloudFormationTemplate) {

    constructor(nimbusState: NimbusState, stage: String): this (
            CloudFormationTemplate(nimbusState, stage),
            CloudFormationTemplate(nimbusState, stage)
    )
}
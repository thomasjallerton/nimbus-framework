package com.nimbusframework.nimbusaws.arm

import com.nimbusframework.nimbuscore.persisted.NimbusState

data class ArmFiles(val createTemplate: ArmTemplate, val updateTemplate: ArmTemplate) {

    constructor(nimbusState: NimbusState, stage: String): this (
            ArmTemplate(nimbusState, stage),
            ArmTemplate(nimbusState, stage)
    )
}
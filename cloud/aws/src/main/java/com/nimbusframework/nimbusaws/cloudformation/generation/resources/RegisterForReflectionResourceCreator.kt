package com.nimbusframework.nimbusaws.cloudformation.generation.resources

import com.nimbusframework.nimbusaws.annotation.annotations.nativeimage.RegisterForReflection
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class RegisterForReflectionResourceCreator(
    roundEnvironment: RoundEnvironment,
    cfDocuments: MutableMap<String, CloudFormationFiles>,
    nimbusState: NimbusState,
    private val classForReflectionService: ClassForReflectionService
) : CloudResourceResourceCreator(
    roundEnvironment,
    cfDocuments,
    nimbusState,
    RegisterForReflection::class.java,
    null
) {

    override fun handleAgnosticType(type: Element) {
        classForReflectionService.addClassForReflection(type.asType())
    }

}

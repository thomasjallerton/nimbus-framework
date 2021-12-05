package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.annotation.annotations.nativeimage.RegisterForReflection
import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.outputs.BucketWebsiteUrlOutput
import com.nimbusframework.nimbusaws.cloudformation.resource.file.FileBucket
import com.nimbusframework.nimbusaws.cloudformation.resource.file.FileStorageBucketPolicy
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition
import com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinitions
import com.nimbusframework.nimbuscore.persisted.ExportInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.WebsiteConfiguration
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

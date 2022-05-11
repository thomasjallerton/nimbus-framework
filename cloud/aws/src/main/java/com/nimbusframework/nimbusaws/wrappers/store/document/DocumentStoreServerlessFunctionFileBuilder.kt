package com.nimbusframework.nimbusaws.wrappers.store.document

import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService
import com.nimbusframework.nimbusaws.cloudformation.model.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.wrappers.store.StoreServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.annotations.function.DocumentStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotations.persistent.StoreEventType
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class DocumentStoreServerlessFunctionFileBuilder<T>(
    processingEnv: ProcessingEnvironment,
    fileBuilderMethodInformation: FileBuilderMethodInformation,
    compilingElement: Element,
    method: StoreEventType,
    clazz: TypeElement,
    classForReflectionService: ClassForReflectionService
) : StoreServerlessFunctionFileBuilder(
    processingEnv,
    fileBuilderMethodInformation,
    compilingElement,
    method,
    clazz,
    DocumentStoreServerlessFunction::class.java.simpleName,
    classForReflectionService
)

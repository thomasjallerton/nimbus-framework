package com.nimbusframework.nimbusaws.wrappers.store.keyvalue

import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbuscore.annotations.function.KeyValueStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotations.persistent.StoreEventType
import com.nimbusframework.nimbusaws.cloudformation.processing.FileBuilderMethodInformation
import com.nimbusframework.nimbusaws.wrappers.store.StoreServerlessFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class KeyValueStoreServerlessFunctionFileBuilder<T>(
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
    KeyValueStoreServerlessFunction::class.java.simpleName,
    classForReflectionService
)

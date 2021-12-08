package com.nimbusframework.nimbusaws.wrappers.store.keyvalue

import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbuscore.annotations.function.KeyValueStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotations.persistent.StoreEventType
import com.nimbusframework.nimbusaws.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbusaws.wrappers.store.StoreServerlessFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class KeyValueStoreServerlessFunctionFileBuilder<T>(
    processingEnv: ProcessingEnvironment,
    methodInformation: MethodInformation,
    compilingElement: Element,
    method: StoreEventType,
    clazz: TypeElement,
    classForReflectionService: ClassForReflectionService
) : StoreServerlessFunctionFileBuilder(
    processingEnv,
    methodInformation,
    compilingElement,
    method,
    clazz,
    KeyValueStoreServerlessFunction::class.java.simpleName,
    classForReflectionService
)

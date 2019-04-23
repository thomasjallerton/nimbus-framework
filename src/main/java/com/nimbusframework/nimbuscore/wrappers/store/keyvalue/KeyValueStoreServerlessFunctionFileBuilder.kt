package com.nimbusframework.nimbuscore.wrappers.store.keyvalue

import com.nimbusframework.nimbuscore.annotation.annotations.function.KeyValueStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotation.annotations.persistent.StoreEventType
import com.nimbusframework.nimbuscore.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.store.StoreServerlessFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class KeyValueStoreServerlessFunctionFileBuilder<T>(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element,
        method: StoreEventType,
        clazz: TypeElement,
        nimbusState: NimbusState
) : StoreServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        compilingElement,
        method,
        clazz,
        KeyValueStoreServerlessFunction::class.java.simpleName,
        nimbusState
)
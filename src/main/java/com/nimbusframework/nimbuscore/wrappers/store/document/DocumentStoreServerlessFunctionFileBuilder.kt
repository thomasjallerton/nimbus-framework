package com.nimbusframework.nimbuscore.wrappers.store.document

import com.nimbusframework.nimbuscore.annotation.annotations.function.DocumentStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotation.annotations.persistent.StoreEventType
import com.nimbusframework.nimbuscore.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.store.StoreServerlessFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class DocumentStoreServerlessFunctionFileBuilder<T>(
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
        DocumentStoreServerlessFunction::class.java.simpleName,
        nimbusState
)
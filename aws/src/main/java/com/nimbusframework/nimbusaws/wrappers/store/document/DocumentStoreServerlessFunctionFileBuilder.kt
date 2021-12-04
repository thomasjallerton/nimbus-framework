package com.nimbusframework.nimbusaws.wrappers.store.document

import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbuscore.annotations.function.DocumentStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotations.persistent.StoreEventType
import com.nimbusframework.nimbusaws.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbusaws.wrappers.store.StoreServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class DocumentStoreServerlessFunctionFileBuilder<T>(
    processingEnv: ProcessingEnvironment,
    methodInformation: MethodInformation,
    compilingElement: Element,
    method: StoreEventType,
    clazz: TypeElement,
    processingData: ProcessingData
) : StoreServerlessFunctionFileBuilder(
    processingEnv,
    methodInformation,
    compilingElement,
    method,
    clazz,
    DocumentStoreServerlessFunction::class.java.simpleName,
    processingData
)

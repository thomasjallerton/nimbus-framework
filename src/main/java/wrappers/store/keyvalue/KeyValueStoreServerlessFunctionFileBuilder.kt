package wrappers.store.keyvalue

import annotation.annotations.function.KeyValueStoreServerlessFunction
import annotation.annotations.persistent.StoreEventType
import cloudformation.processing.MethodInformation
import wrappers.store.StoreServerlessFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class KeyValueStoreServerlessFunctionFileBuilder<T>(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element,
        method: StoreEventType,
        clazz: TypeElement
) : StoreServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        compilingElement,
        method,
        clazz,
        KeyValueStoreServerlessFunction::class.java.simpleName
)
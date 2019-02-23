package wrappers.store.document

import annotation.annotations.function.DocumentStoreServerlessFunction
import annotation.annotations.persistent.StoreUpdate
import cloudformation.processing.MethodInformation
import wrappers.store.StoreServerlessFunctionFileBuilder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class DocumentStoreServerlessFunctionFileBuilder<T>(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element,
        method: StoreUpdate,
        clazz: TypeElement
) : StoreServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        compilingElement,
        method,
        clazz,
        DocumentStoreServerlessFunction::class.java.simpleName
)
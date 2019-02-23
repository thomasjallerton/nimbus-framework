package wrappers.store.keyvalue

import annotation.annotations.function.KeyValueStoreServerlessFunction
import annotation.annotations.persistent.StoreUpdate
import annotation.cloudformation.processing.MethodInformation
import clients.dynamo.DynamoStreamParser
import wrappers.ServerlessFunctionFileBuilder
import wrappers.store.StoreServerlessFunctionFileBuilder
import wrappers.store.models.DynamoRecords
import wrappers.store.models.DynamoUpdate
import wrappers.store.models.StoreEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class KeyValueStoreServerlessFunctionFileBuilder<T>(
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
        KeyValueStoreServerlessFunction::class.java.simpleName
)
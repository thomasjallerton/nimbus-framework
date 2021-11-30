package com.nimbusframework.nimbusaws.wrappers.store

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.nimbusframework.nimbusaws.clients.dynamo.DynamoStreamParser
import com.nimbusframework.nimbuscore.annotations.persistent.StoreEventType
import com.nimbusframework.nimbusaws.cloudformation.processing.MethodInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbusaws.wrappers.ServerlessFunctionFileBuilder
import com.nimbusframework.nimbuscore.eventabstractions.StoreEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

abstract class StoreServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element,
        private val method: StoreEventType,
        private val clazz: TypeElement,
        private val functionName: String,
        nimbusState: NimbusState
) : ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        functionName,
        StoreEvent::class.java,
        compilingElement,
        DynamodbEvent::class.java,
        Void::class.java,
        nimbusState
) {
    override fun getGeneratedClassName(): String {
        return "$functionName${methodInformation.className}${methodInformation.methodName}"
    }

    private val eventMapperSimpleName = DynamoStoreEventMapper::class.java.simpleName

    override fun isValidFunction(functionParams: FunctionParams) {
        if (functionParams.inputParam.type != null && functionParams.inputParam.type.toString() != clazz.toString()) {
            compilationError("TYPES ARE NOT CORRECT, EXPECTED $clazz but method uses ${functionParams.inputParam.type}")
        }

        val errorPrefix = "Incorrect $functionName annotated method parameters."
        val eventSimpleName = StoreEvent::class.java.simpleName

        if (method == StoreEventType.INSERT || method == StoreEventType.REMOVE) {
            if (methodInformation.parameters.size > 2) {
                compilationError("$errorPrefix Too many arguments, can have at most two: T input, $eventSimpleName event.")
            } else if (methodInformation.parameters.size == 2) {
                if (functionParams.eventParam.doesNotExist()) {
                    compilationError("$errorPrefix Can't have two data input types. Function can have at most two parameters: T input, $eventSimpleName event.")
                } else if (functionParams.inputParam.doesNotExist()) {
                    compilationError("$errorPrefix Can't have two event input types. Function can have at most two parameters: T input, $eventSimpleName event.")
                }
            }
        } else {
            if (methodInformation.parameters.size > 3) {
                compilationError("$errorPrefix Too many arguments, can have at most three: T oldEvent, T newEvent, $eventSimpleName event.")
            } else if (methodInformation.parameters.size == 2) {
                if (functionParams.eventParam.doesNotExist() && methodInformation.parameters[0] != methodInformation.parameters[1]) {
                    compilationError("$errorPrefix Wrong input arguments, cannot have two different types of data inputs. " +
                            "Can have at most three arguments: T oldEvent, T newEvent, $eventSimpleName event. (Your T's were not the same type)")
                } else if (functionParams.inputParam.doesNotExist()) {
                    compilationError("$errorPrefix Can't have two event input types. Function can have at most three parameters: T oldEvent, T newEvent, $eventSimpleName event.")
                }
            } else if (methodInformation.parameters.size == 3) {
                if (functionParams.eventParam.doesNotExist()) {
                    compilationError("$errorPrefix Can't have three data input types. Function can have at most three parameters: T oldEvent, T newEvent, $eventSimpleName event.")
                } else if (functionParams.inputParam.doesNotExist()) {
                    compilationError("$errorPrefix Can't have three event input types. Function can have at most three parameters: T oldEvent, T newEvent, $eventSimpleName event.")
                } else if (functionParams.eventParam.index != 2) {
                    compilationError("$errorPrefix If three parameters then event input needs to be the final one. Need exact order: T oldEvent, T newEvent, $eventSimpleName event.")
                } else if (functionParams.eventParam.index == 2 && methodInformation.parameters[0] != methodInformation.parameters[1]) {
                    compilationError("$errorPrefix Wrong input arguments, cannot have two different types of data inputs. " +
                            "Can have at most three arguments: T oldEvent, T newEvent, $eventSimpleName event. (Your T's were not the same type)")
                }
            }
        }

        if (functionParams.eventParam.type != null && isAListType(functionParams.eventParam.type!!)) {
            compilationError("$errorPrefix Cannot have a list of $eventSimpleName for a $functionName")
        }
    }

    override fun writeImports() {

        write("import ${DynamoStreamParser::class.qualifiedName};")
        write("import ${StoreEvent::class.qualifiedName};")
        write("import ${DynamoStoreEventMapper::class.qualifiedName};")

        write()
    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        if (inputParam.type != null) {
            write("DynamodbEvent.DynamodbStreamRecord record = input.getRecords().get(0);")
            write("$eventSimpleName event = $eventMapperSimpleName.getStoreEvent(record, requestId);")
            write("if (!\"${method.name}\".equals(event.getEventName())) return null;")

            write("DynamoStreamParser<${inputParam.type}> parser = DynamoStreamParser.of(${inputParam.type}.class);")
            write("${inputParam.type} parsedNewItem = parser.toObjectFromLambda(record.getDynamodb().getNewImage());")
            write("${inputParam.type} parsedOldItem = parser.toObjectFromLambda(record.getDynamodb().getOldImage());")
        }

        val methodName = methodInformation.methodName
        when {
            inputParam.doesNotExist() && eventParam.doesNotExist() -> write("handler.$methodName();")
            inputParam.type == null -> write("handler.$methodName(event);")
            methodInformation.parameters.size == 1 -> handleOneParam(methodName)
            methodInformation.parameters.size == 2 -> handleTwoParams(methodName, eventParam)
            methodInformation.parameters.size == 3 -> write("handler.$methodName(parsedOldItem, parsedNewItem, event);")
        }
        write("return null;")
    }

    private fun handleOneParam(methodName: String) {
        when (method) {
            StoreEventType.INSERT -> write("handler.$methodName(parsedNewItem);")
            StoreEventType.REMOVE -> write("handler.$methodName(parsedOldItem);")
            StoreEventType.MODIFY -> write("handler.$methodName(parsedNewItem);")
        }
    }

    private fun handleTwoParams(methodName: String, eventParam: Param) {
        when (method) {
            StoreEventType.INSERT -> {
                if (eventParam.index == 0) {
                    write("handler.$methodName(event, parsedNewItem);")
                } else {
                    write("handler.$methodName(parsedNewItem, event);")
                }
            }
            StoreEventType.REMOVE -> {
                if (eventParam.index == 0) {
                    write("handler.$methodName(event, parsedOldItem);")
                } else {
                    write("handler.$methodName(parsedOldItem, event);")
                }
            }
            StoreEventType.MODIFY -> {
                when {
                    eventParam.doesNotExist() -> write("handler.$methodName(parsedOldItem, parsedNewItem);")
                    eventParam.index == 0 -> write("handler.$methodName(event, parsedNewItem);")
                    else -> write("handler.$methodName(parsedNewItem, event);")
                }
            }
        }
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
        write("return null;")
    }
}

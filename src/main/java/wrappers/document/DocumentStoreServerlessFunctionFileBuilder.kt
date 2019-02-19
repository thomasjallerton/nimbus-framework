package wrappers.document

import annotation.annotations.function.DocumentStoreServerlessFunction
import annotation.annotations.persistent.StoreUpdate
import annotation.models.processing.MethodInformation
import clients.dynamo.DynamoStreamParser
import wrappers.ServerlessFunctionFileBuilder
import wrappers.document.models.DynamoRecords
import wrappers.document.models.DynamoUpdate
import wrappers.document.models.StoreEvent
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class DocumentStoreServerlessFunctionFileBuilder(
        processingEnv: ProcessingEnvironment,
        methodInformation: MethodInformation,
        compilingElement: Element,
        private val method: StoreUpdate
) : ServerlessFunctionFileBuilder(
        processingEnv,
        methodInformation,
        DocumentStoreServerlessFunction::class.java.simpleName,
        StoreEvent(),
        compilingElement
) {
    override fun getGeneratedClassName(): String {
        return "DocumentStoreServerlessFunction${methodInformation.className}${methodInformation.methodName}"
    }

    override fun isValidFunction(functionParams: FunctionParams) {
        val errorPrefix = "Incorrect DocumentStoreServerlessFunction annotated method parameters."
        val eventSimpleName = StoreEvent::class.java.simpleName

        if (method == StoreUpdate.INSERT || method == StoreUpdate.REMOVE) {
            if (methodInformation.parameters.size > 2) {
                compilationError("$errorPrefix Too many arguments, can have at most two: T input, $eventSimpleName event.")
            } else if (methodInformation.parameters.size == 2) {
                if (functionParams.eventParam.isEmpty()) {
                    compilationError("$errorPrefix Can't have two data input types. Function can have at most two parameters: T input, $eventSimpleName event.")
                } else if (functionParams.inputParam.isEmpty()) {
                    compilationError("$errorPrefix Can't have two event input types. Function can have at most two parameters: T input, $eventSimpleName event.")
                }
            }
        } else {
            if (methodInformation.parameters.size > 3) {
                compilationError("$errorPrefix Too many arguments, can have at most three: T oldEvent, T newEvent, $eventSimpleName event.")
            } else if (methodInformation.parameters.size == 2) {
                if (functionParams.eventParam.isEmpty() && methodInformation.parameters[0] != methodInformation.parameters[1]) {
                    compilationError("$errorPrefix Wrong input arguments, cannot have two different types of data inputs. " +
                            "Can have at most three arguments: T oldEvent, T newEvent, $eventSimpleName event. (Your T's were not the same type)")
                } else if (functionParams.inputParam.isEmpty()) {
                    compilationError("$errorPrefix Can't have two event input types. Function can have at most three parameters: T oldEvent, T newEvent, $eventSimpleName event.")
                }
            } else if (methodInformation.parameters.size == 3) {
                if (functionParams.eventParam.isEmpty()) {
                    compilationError("$errorPrefix Can't have three data input types. Function can have at most three parameters: T oldEvent, T newEvent, $eventSimpleName event.")
                } else if (functionParams.inputParam.isEmpty()) {
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
            compilationError("$errorPrefix Cannot have a list of $eventSimpleName for a DocumentStoreServerlessFunction")
        }
    }

    override fun writeOutput() {}

    override fun writeImports() {
        write()

        write("import com.fasterxml.jackson.databind.ObjectMapper;")
        write("import com.amazonaws.services.lambda.runtime.Context;")
        write("import java.io.*;")
        write("import java.util.stream.Collectors;")
        if (methodInformation.qualifiedName.isNotBlank()) {
            write("import ${methodInformation.qualifiedName}.${methodInformation.className};")
        }
        write("import ${DynamoRecords::class.qualifiedName};")
        write("import ${DynamoUpdate::class.qualifiedName};")
        write("import ${DynamoStreamParser::class.qualifiedName};")
        write("import ${StoreEvent::class.qualifiedName};")

        write()
    }

    override fun writeInputs(param: Param) {
        write("System.out.println(jsonString);")

        write("DynamoRecords records = objectMapper.readValue(jsonString, DynamoRecords.class);")

        if (param.type != null) {
            write("StoreEvent event = records.getRecord().get(0);")
            write("if (!\"${method.name}\".equals(event.getEventName())) return;")
            write("DynamoUpdate update = event.getDynamodb();")
            write("DynamoStreamParser<${param.type}> parser = new DynamoStreamParser(${param.type}.class);")
            write("${param.type} parsedNewItem = parser.toObject(update.getNewImage());")
            write("${param.type} parsedOldItem = parser.toObject(update.getOldImage());")
        }

    }

    override fun writeFunction(inputParam: Param, eventParam: Param) {
        if (methodInformation.returnType.toString() != "void") {
            messager.printMessage(Diagnostic.Kind.WARNING, "The function ${methodInformation.className}::" +
                    "${methodInformation.methodName} has a return type which will be unused. It can be removed")
        }

        val methodName = methodInformation.methodName
        when {
            inputParam.isEmpty() && eventParam.isEmpty() -> write("handler.$methodName();")
            inputParam.type == null -> write("handler.$methodName(event);")
            eventParam.type == null && method == StoreUpdate.REMOVE -> write("handler.$methodName(parsedOldItem);")
            eventParam.type == null && method == StoreUpdate.INSERT -> write("handler.$methodName(parsedNewItem);")
            methodInformation.parameters.size == 2 -> handleTwoParams(methodName, inputParam, eventParam)
            methodInformation.parameters.size == 3 -> write("handler.$methodName(parsedOldItem, parsedNewItem, event);")
        }
    }

    private fun handleTwoParams(methodName: String, inputParam: Param, eventParam: Param) {
        if (method == StoreUpdate.INSERT) {
            if (eventParam.index == 0) {
                write("handler.$methodName(event, parsedNewItem);")
            } else {
                write("handler.$methodName(parsedNewItem, event);")
            }
        } else if (method == StoreUpdate.REMOVE) {
            if (eventParam.index == 0) {
                write("handler.$methodName(event, parsedOldItem);")
            } else {
                write("handler.$methodName(parsedOldItem, event);")
            }
        } else if (method == StoreUpdate.MODIFY) {
            when {
                eventParam.isEmpty() -> write("handler.$methodName(parsedOldItem, parsedNewItem);")
                eventParam.index == 0 -> write("handler.$methodName(event, parsedNewItem);")
                else -> write("handler.$methodName(parsedNewItem, event);")
            }
        }
    }

    override fun writeHandleError() {
        write("e.printStackTrace();")
    }
}
package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.extensions.system.withEnvironment
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

class EnvironmentVariablesProcessorTest: AnnotationSpec() {

    private lateinit var environmentVariablesProcessor: EnvironmentVariablesProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var elements: Elements
    private lateinit var messager: Messager

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)

        val compileState = CompileStateService("handlers/UsesEnvironmentVariableHandler.java")
        elements = compileState.elements

        HttpFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())
        HttpFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        environmentVariablesProcessor = EnvironmentVariablesProcessor(messager)
    }

    @Test
    fun correctlySetsVariableFromString() {
        val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("esEnvironmentVariableHandlerfuncFunction") as FunctionResource

        environmentVariablesProcessor.handleUseResources(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[1], functionResource)

        functionResource.usesClient(ClientType.EnvironmentVariable) shouldBe true
        functionResource.getStrEnvValue("TEST_KEY") shouldBe "TEST_VALUE"
    }

    @Test
    fun handlesLocalVariableBeingNull() {
        val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("esEnvironmentVariableHandlerfunc2Function") as FunctionResource

        environmentVariablesProcessor.handleUseResources(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[2], functionResource)

        verify { messager.printMessage(Diagnostic.Kind.ERROR, any()) }

        functionResource.usesClient(ClientType.EnvironmentVariable) shouldBe true
        functionResource.getStrEnvValue("TEST_KEY") shouldBe "\${TEST_VALUE}"
    }

    @Test
    fun handlesLocalVariableBeingSet() {
        withEnvironment("TEST_VALUE", "TEST_ENV_VAL") {

            val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("esEnvironmentVariableHandlerfunc2Function") as FunctionResource

            environmentVariablesProcessor.handleUseResources(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[2], functionResource)

            functionResource.usesClient(ClientType.EnvironmentVariable) shouldBe true
            functionResource.getStrEnvValue("TEST_KEY") shouldBe "TEST_ENV_VAL"
        }
    }

}
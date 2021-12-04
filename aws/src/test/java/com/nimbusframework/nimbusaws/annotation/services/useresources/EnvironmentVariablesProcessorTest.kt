package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.tools.Diagnostic

class EnvironmentVariablesProcessorTest : AnnotationSpec() {

    private lateinit var environmentVariablesProcessor: EnvironmentVariablesProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var processingData: ProcessingData
    private lateinit var nimbusState: NimbusState
    private lateinit var messager: Messager
    private lateinit var compileState: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        processingData = ProcessingData(nimbusState)
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)

        compileState = CompileStateService("handlers/UsesEnvironmentVariableHandler.java")

        environmentVariablesProcessor = EnvironmentVariablesProcessor(processingData.nimbusState, messager)
    }

    @Test
    fun correctlySetsVariableFromString() {
        compileState.compileObjects {
            val elements = it.elementUtils
            HttpFunctionResourceCreator(cfDocuments, processingData, it, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState))
            HttpFunctionResourceCreator(cfDocuments, processingData, it, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, nimbusState))

            val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("esEnvironmentVariableHandlerfuncFunction") as FunctionResource

            environmentVariablesProcessor.handleUseResources(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[1], functionResource)

            functionResource.usesClient(ClientType.EnvironmentVariable) shouldBe true
            functionResource.getStrEnvValue("TEST_KEY") shouldBe "TEST_VALUE"
        }
        compileState.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun handlesLocalVariableBeingNull() {
        compileState.compileObjects {
            val elements = it.elementUtils
            HttpFunctionResourceCreator(cfDocuments, processingData, it, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState))
            HttpFunctionResourceCreator(cfDocuments, processingData, it, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, nimbusState))
            val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("esEnvironmentVariableHandlerfunc2Function") as FunctionResource

            environmentVariablesProcessor.handleUseResources(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[2], functionResource)

            verify { messager.printMessage(Diagnostic.Kind.ERROR, any()) }

            functionResource.usesClient(ClientType.EnvironmentVariable) shouldBe true
            functionResource.getStrEnvValue("TEST_KEY") shouldBe "\${TEST_VALUE}"
        }
        compileState.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun handlesLocalVariableBeingSet() {
        withEnvironment("TEST_VALUE", "TEST_ENV_VAL") {
            compileState.compileObjects {
                val elements = it.elementUtils
                HttpFunctionResourceCreator(cfDocuments, processingData, it, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState))
                HttpFunctionResourceCreator(cfDocuments, processingData, it, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, nimbusState))

                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("esEnvironmentVariableHandlerfunc2Function") as FunctionResource

                environmentVariablesProcessor.handleUseResources(elements.getTypeElement("handlers.UsesEnvironmentVariableHandler").enclosedElements[2], functionResource)

                functionResource.usesClient(ClientType.EnvironmentVariable) shouldBe true
                functionResource.getStrEnvValue("TEST_KEY") shouldBe "TEST_ENV_VAL"
            }
        }
        compileState.status shouldBe Compilation.Status.SUCCESS
    }
}

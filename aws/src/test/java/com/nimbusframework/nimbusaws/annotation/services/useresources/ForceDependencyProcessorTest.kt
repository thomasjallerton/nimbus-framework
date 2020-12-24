package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment

class ForceDependencyProcessorTest: AnnotationSpec() {

    private lateinit var forceDependencyProcessor: ForceDependencyProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var messager: Messager
    private lateinit var compileState: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)

        compileState = CompileStateService("handlers/ForceDependencyHandler.java")

        forceDependencyProcessor = ForceDependencyProcessor(nimbusState)
    }

    @Test
    fun correctlySetsVariableFromString() {
        compileState.compileObjects {
            val elements = it.elementUtils
            HttpFunctionResourceCreator(cfDocuments, nimbusState, it, mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.ForceDependencyHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

            val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("ForceDependencyHandlerfuncFunction") as FunctionResource

            forceDependencyProcessor.handleUseResources(elements.getTypeElement("handlers.ForceDependencyHandler").enclosedElements[1], functionResource)

            functionResource.containsDependency("com.test.test") shouldBe true
            functionResource.containsDependency("com.example.test") shouldBe true
        }
        compileState.status shouldBe Compilation.Status.SUCCESS
    }

}
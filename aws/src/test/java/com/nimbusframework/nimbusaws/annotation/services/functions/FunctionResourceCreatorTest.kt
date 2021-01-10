package com.nimbusframework.nimbusaws.annotation.services.functions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.decorators.FunctionDecoratorHandler
import com.nimbusframework.nimbusaws.annotation.services.functions.decorators.KeepWarmDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.decorator.KeepWarm
import com.nimbusframework.nimbuscore.annotations.function.repeatable.HttpServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class FunctionResourceCreatorTest : AnnotationSpec() {

    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var functionEnvironmentService: FunctionEnvironmentService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        functionEnvironmentService = mockk(relaxed = true)
    }

    @Test
    fun correctlyProcessesFunction() {
        val functionInformation = mockk<FunctionInformation>()
        val functionDecoratorHandler = mockk<FunctionDecoratorHandler>(relaxed = true)

        val underTest = DummyFunctionResourceCreator(
            cfDocuments,
            nimbusState,
            mockk(),
            functionDecoratorHandler,
            mockk(relaxed = true),
            listOf(functionInformation)
        )

        val typeMock = mockk<Element>()
        every { roundEnvironment.getElementsAnnotatedWithAny(any<Set<Class<out Annotation>>>()) } returns setOf(typeMock)

        val results = underTest.handle(roundEnvironment, functionEnvironmentService)

        verify { functionDecoratorHandler.handleDecorator(typeMock, listOf(functionInformation)) }

        results.size shouldBe 1
        results shouldContain functionInformation
    }

    class DummyFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment,
        decoratorHandler: FunctionDecoratorHandler,
        messager: Messager,
        private val returnedFunctionInformation: List<FunctionInformation>
    ) : FunctionResourceCreator(
        cfDocuments,
        nimbusState,
        processingEnv,
        setOf(decoratorHandler),
        messager,
        HttpServerlessFunction::class.java,
        HttpServerlessFunctions::class.java
    ) {

        override fun handleElement(
            type: Element,
            functionEnvironmentService: FunctionEnvironmentService
        ): List<FunctionInformation> {
            return returnedFunctionInformation
        }

    }
}
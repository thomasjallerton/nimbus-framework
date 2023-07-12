package com.nimbusframework.nimbusaws.cloudformation.generation.abstractions

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.AwsMethodInformation
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.basicfunction.BasicFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction
import com.nimbusframework.nimbuscore.annotations.function.repeatable.HttpServerlessFunctions
import com.nimbusframework.nimbuscore.persisted.HandlerInformation
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
    private lateinit var processingData: ProcessingData

    @BeforeEach
    fun setup() {
        processingData = ProcessingData(NimbusState(defaultStages = listOf("dev")))
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
    }

    @Test
    fun correctlyProcessesFunction() {
        val functionInformation = mockk<FunctionInformation>()
        val functionDecoratorHandler = mockk<FunctionDecoratorHandler>(relaxed = true)

        val underTest = DummyFunctionResourceCreator(
            cfDocuments,
            processingData,
            mockk(),
            functionDecoratorHandler,
            mockk(relaxed = true),
            listOf(functionInformation)
        )

        val typeMock = mockk<Element>()
        every { roundEnvironment.getElementsAnnotatedWithAny(any<Set<Class<out Annotation>>>()) } returns setOf(typeMock)

        val results = underTest.handle(roundEnvironment, mockk(relaxed = true))

        verify { functionDecoratorHandler.handleDecorator(typeMock, listOf(functionInformation)) }

        results.size shouldBe 1
        results shouldContain functionInformation
    }

    @Test
    fun correctlyProcessesCustomFunction() {
        val compileStateService = CompileStateService("models/NotificationTopic.java", "handlers/basic/BasicCustomFunctionHandler.java")
        compileStateService.compileObjects {
            val basicFunctionResourceCreator = BasicFunctionResourceCreator(cfDocuments, processingData, ClassForReflectionService(processingData, it.typeUtils), it, setOf(), it.messager)

            val basicFunctionElem = it.elementUtils.getTypeElement("handlers.basic.BasicCustomFunctionHandler").enclosedElements[1]
            val result = basicFunctionResourceCreator.handleElement(basicFunctionElem, FunctionEnvironmentService(cfDocuments, processingData))

            cfDocuments["dev"] shouldNotBe null
            val resources = cfDocuments["dev"]!!.updateTemplate.resources

            // then
            // ... created function as custom function
            val functionResource = resources.getFunction("handlers.basic.BasicCustomFunctionHandler", "func")!!
            functionResource.handlerInformation shouldBe HandlerInformation(
                "",
                "handler",
                "\${handlers_basic_BasicCustomFunctionHandler_func}",
                "testfile",
                "provided"
            )

            result shouldHaveSize 1
            val functionInformation = result[0]
            functionInformation.awsMethodInformation shouldBe AwsMethodInformation(
                "handlers.basic",
                "BasicServerlessFunctionBasicCustomFunctionHandlerfunc",
                Void::class.qualifiedName!!,
                Void::class.qualifiedName!!
            )
        }
    }

    class DummyFunctionResourceCreator(
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        processingData: ProcessingData,
        processingEnv: ProcessingEnvironment,
        decoratorHandler: FunctionDecoratorHandler,
        messager: Messager,
        private val returnedFunctionInformation: List<FunctionInformation>
    ) : FunctionResourceCreator(
        cfDocuments,
        processingData,
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

package com.nimbusframework.nimbusaws.annotation.services.functions.decorators

import com.nimbusframework.nimbusaws.annotation.processor.AwsMethodInformation
import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.KeepWarmDecoratorHandler
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.annotations.function.decorator.KeepWarm
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class KeepWarmDecoratorHandlerTest : AnnotationSpec() {

    private var roundEnvironment: RoundEnvironment = mockk()
    private lateinit var nimbusState: NimbusState
    private lateinit var functionEnvironmentService: FunctionEnvironmentService

    private lateinit var underTest: KeepWarmDecoratorHandler

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        functionEnvironmentService = mockk(relaxed = true)
        underTest = KeepWarmDecoratorHandler(nimbusState, functionEnvironmentService)
    }

    @Test
    fun correctlyProcessesNoKeepWarmAnnotation() {
        val functionResource = mockk<FunctionResource>()
        val functionInformation = FunctionInformation(mockk(), functionResource, AwsMethodInformation(), true)

        every { functionResource.stage } returns "not default"

        nimbusState = NimbusState(keepWarmStages = listOf("test"))

        val typeMock = mockk<Element>()
        every { typeMock.getAnnotation(KeepWarm::class.java) } returns null
        every { roundEnvironment.getElementsAnnotatedWithAny(any<Set<Class<out Annotation>>>()) } returns setOf(typeMock)

        underTest.handleDecorator(typeMock, listOf(functionInformation))

        verify(exactly = 0) { functionEnvironmentService.newCronTrigger(any(), eq(functionResource)) }
    }

    @Test
    fun correctlyProcessesKeepWarmAnnotation() {
        val functionResource = mockk<FunctionResource>()
        val functionInformation = FunctionInformation(mockk(), functionResource, AwsMethodInformation(), true)

        val typeMock = mockk<Element>()
        every { typeMock.getAnnotation(KeepWarm::class.java) } returns mockk()
        every { roundEnvironment.getElementsAnnotatedWithAny(any<Set<Class<out Annotation>>>()) } returns setOf(typeMock)

        underTest.handleDecorator(typeMock, listOf(functionInformation))

        verify { functionEnvironmentService.newCronTrigger(any(), eq(functionResource)) }
    }

    @Test
    fun correctlyProcessesKeepWarmAnnotationButCannotBeKeptWarm() {
        val functionResource = mockk<FunctionResource>()
        val functionInformation = FunctionInformation(mockk(), functionResource, AwsMethodInformation(), false)

        val typeMock = mockk<Element>()
        every { typeMock.getAnnotation(KeepWarm::class.java) } returns mockk()
        every { roundEnvironment.getElementsAnnotatedWithAny(any<Set<Class<out Annotation>>>()) } returns setOf(typeMock)

        underTest.handleDecorator(typeMock, listOf(functionInformation))

        verify(exactly = 0) { functionEnvironmentService.newCronTrigger(any(), eq(functionResource)) }
    }

    @Test
    fun correctlyProcessesKeepWarmState() {
        val functionResource = mockk<FunctionResource>()
        val functionInformation = FunctionInformation(mockk(), functionResource, AwsMethodInformation(), true)

        every { functionResource.stage } returns "test"

        nimbusState = NimbusState(keepWarmStages = listOf("test"))
        underTest = KeepWarmDecoratorHandler(nimbusState, functionEnvironmentService)

        val typeMock = mockk<Element>()
        every { typeMock.getAnnotation(KeepWarm::class.java) } returns null
        every { roundEnvironment.getElementsAnnotatedWithAny(any<Set<Class<out Annotation>>>()) } returns setOf(typeMock)

        underTest.handleDecorator(typeMock, listOf(functionInformation))

        verify { functionEnvironmentService.newCronTrigger(any(), eq(functionResource)) }
    }

    @Test
    fun correctlyProcessesKeepWarmStateButCannotBeKeptWarm() {
        val functionResource = mockk<FunctionResource>()
        val functionInformation = FunctionInformation(mockk(), functionResource, AwsMethodInformation(), false)

        every { functionResource.stage } returns "test"

        nimbusState = NimbusState(keepWarmStages = listOf("test"))
        underTest = KeepWarmDecoratorHandler(nimbusState, functionEnvironmentService)

        val typeMock = mockk<Element>()
        every { typeMock.getAnnotation(KeepWarm::class.java) } returns null
        every { roundEnvironment.getElementsAnnotatedWithAny(any<Set<Class<out Annotation>>>()) } returns setOf(typeMock)

        underTest.handleDecorator(typeMock, listOf(functionInformation))

        verify(exactly = 0) { functionEnvironmentService.newCronTrigger(any(), eq(functionResource)) }
    }
}

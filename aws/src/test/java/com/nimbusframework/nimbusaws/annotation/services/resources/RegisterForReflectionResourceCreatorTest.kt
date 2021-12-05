package com.nimbusframework.nimbusaws.annotation.services.resources

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesKeyValueStoreProcessor
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

internal class RegisterForReflectionResourceCreatorTest: AnnotationSpec() {

    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var processingData: ProcessingData
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState(customRuntime = true)
        processingData = ProcessingData(nimbusState)
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileStateService = CompileStateService("models/People.java", "models/RegisterNestedClass.java", "models/RegisterNestedNestedClass.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, file: String, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils
        val classForReflectionService = ClassForReflectionService(processingData, processingEnvironment.typeUtils)
        RegisterForReflectionResourceCreator(roundEnvironment, cfDocuments, processingData.nimbusState, classForReflectionService).handleAgnosticType(elements.getTypeElement(file))

        toRun()
    }

    @Test
    fun handlesOnTopLevelClass() {
        compileStateService.compileObjects {
            setup(it, "models.People") {
                processingData.classesForReflection shouldContain "models.People"
                processingData.classesForReflection shouldContain "models.NestedPerson"
                processingData.classesForReflection shouldContain "models.Person"
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun handlesOnNestedClass() {
        compileStateService.compileObjects {
            setup(it, "models.RegisterNestedClass.RegisterNestedClassIdentifier") {
                processingData.classesForReflection shouldContain "models.RegisterNestedClass\$RegisterNestedClassIdentifier"
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun handlesNestedNestedClass() {
        compileStateService.compileObjects {
            setup(it, "models.RegisterNestedNestedClass.Nested.SuperNested") {
                processingData.classesForReflection shouldContain "models.RegisterNestedNestedClass\$Nested\$SuperNested"
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }


}

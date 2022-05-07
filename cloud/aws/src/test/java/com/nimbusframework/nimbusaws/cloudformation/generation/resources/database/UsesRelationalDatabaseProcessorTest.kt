package com.nimbusframework.nimbusaws.cloudformation.generation.resources.database

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.annotations.database.ParsedDatabaseConfig
import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedQueueDefinition
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.database.RelationalDatabaseResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.database.UsesRelationalDatabaseProcessor
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.database.RdsResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.ec2.SecurityGroupResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.ec2.Vpc
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.queue.QueueResource
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class UsesRelationalDatabaseProcessorTest: AnnotationSpec() {

    private lateinit var usesRelationalDatabaseProcessor: UsesRelationalDatabaseProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var processingData: ProcessingData
    private lateinit var messager: Messager
    private lateinit var compileStateService: CompileStateService
    private lateinit var resourceFinder: ResourceFinder

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        processingData = ProcessingData(nimbusState)
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)

        compileStateService = CompileStateService("models/RelationalDatabaseModel.java", "handlers/UsesRDBHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils
        RelationalDatabaseResourceCreator(roundEnvironment, cfDocuments, nimbusState).handleAgnosticType(elements.getTypeElement("models.RelationalDatabaseModel"))

        HttpFunctionResourceCreator(cfDocuments, processingData, mockk(relaxed = true), processingEnvironment, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesRDBHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, processingData))

        resourceFinder = ResourceFinder(cfDocuments, processingEnvironment, nimbusState)
        usesRelationalDatabaseProcessor = UsesRelationalDatabaseProcessor(resourceFinder, nimbusState)
        toRun()
    }

    @Test
    fun correctlySetsPermissions() {
        compileStateService.compileObjects {
            setup(it) {

                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesRDBHandlerfuncFunction") as FunctionResource

                usesRelationalDatabaseProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesRDBHandler").enclosedElements[1], functionResource)

                functionResource.getJsonEnvValue("testRelationalDatabase_CONNECTION_URL") shouldNotBe null
                functionResource.getStrEnvValue("testRelationalDatabase_USERNAME") shouldBe "username"
                functionResource.getStrEnvValue("testRelationalDatabase_PASSWORD") shouldBe "password"
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

}

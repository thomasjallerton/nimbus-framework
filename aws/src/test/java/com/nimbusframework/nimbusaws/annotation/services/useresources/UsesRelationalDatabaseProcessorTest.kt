package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.resources.RelationalDatabaseResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class UsesRelationalDatabaseProcessorTest: AnnotationSpec() {

    private lateinit var usesRelationalDatabaseProcessor: UsesRelationalDatabaseProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var messager: Messager
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)

        compileStateService = CompileStateService("models/RelationalDatabaseModel.java", "handlers/UsesRDBHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils
        RelationalDatabaseResourceCreator(roundEnvironment, cfDocuments, nimbusState).handleAgnosticType(elements.getTypeElement("models.RelationalDatabaseModel"))

        HttpFunctionResourceCreator(cfDocuments, nimbusState, processingEnvironment, mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesRDBHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState))

        usesRelationalDatabaseProcessor = UsesRelationalDatabaseProcessor(cfDocuments, processingEnvironment, nimbusState)
        toRun()
    }

    @Test
    fun correctlySetsPermissions() {
        compileStateService.compileObjects {
            setup(it) {
                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesRDBHandlerfuncFunction") as FunctionResource

                usesRelationalDatabaseProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesRDBHandler").enclosedElements[1], functionResource)

                functionResource.usesClient(ClientType.Database) shouldBe true
                functionResource.getJsonEnvValue("RdsInstancetestRelationalDatabase_CONNECTION_URL") shouldNotBe null
                functionResource.getStrEnvValue("RdsInstancetestRelationalDatabase_USERNAME") shouldBe "username"
                functionResource.getStrEnvValue("RdsInstancetestRelationalDatabase_PASSWORD") shouldBe "password"

                functionResource.containsDependency(com.mysql.cj.jdbc.Driver::class.java.canonicalName) shouldBe true
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

}
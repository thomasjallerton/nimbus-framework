package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.functions.QueueFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

class UsesQueueProcessorTest: AnnotationSpec() {

    private lateinit var usesQueueProcessor: UsesQueueProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var elements: Elements
    private lateinit var iamRoleResource: IamRoleResource
    private lateinit var messager: Messager

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)

        val compileState = CompileStateService("handlers/QueueHandlers.java", "handlers/UsesQueueHandler.java")
        elements = compileState.elements

        QueueFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment).handleElement(elements.getTypeElement("handlers.QueueHandlers").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        HttpFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesQueueHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())
        HttpFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesQueueHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        iamRoleResource = cfDocuments["dev"]!!.updateTemplate.resources.get("IamRoleUsesQueueHandlerfuncExecution") as IamRoleResource
        usesQueueProcessor = UsesQueueProcessor(cfDocuments, nimbusState, messager)
    }

    @Test
    fun correctlySetsPermissions() {
        val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesQueueHandlerfuncFunction") as FunctionResource

        usesQueueProcessor.handleUseResources(elements.getTypeElement("handlers.UsesQueueHandler").enclosedElements[1], functionResource)
        val queueResource = cfDocuments["dev"]!!.updateTemplate.resources.get("NimbusSQSQueuemessageQueue")!!

        functionResource.usesClient(ClientType.Queue) shouldBe true
        functionResource.getStrEnvValue("NIMBUS_STAGE") shouldBe "dev"
        functionResource.getJsonEnvValue("NIMBUS_QUEUE_URL_ID_MESSAGEQUEUE") shouldNotBe null

        iamRoleResource.allows("sqs:SendMessage", queueResource) shouldBe true
    }

    @Test
    fun throwsErrorIfQueueDoesNotExist() {
        val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesQueueHandlerfunc2Function") as FunctionResource

        usesQueueProcessor.handleUseResources(elements.getTypeElement("handlers.UsesQueueHandler").enclosedElements[2], functionResource)

        verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
    }
}
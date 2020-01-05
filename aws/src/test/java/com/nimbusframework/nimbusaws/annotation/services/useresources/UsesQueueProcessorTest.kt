package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.functions.QueueFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.resources.QueueResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbusaws.cloudformation.resource.queue.QueueResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

class UsesQueueProcessorTest: AnnotationSpec() {

    private lateinit var usesQueueProcessor: UsesQueueProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var iamRoleResource: IamRoleResource
    private lateinit var messager: Messager
    private lateinit var compileStateService: CompileStateService
    private lateinit var resourceFinder: ResourceFinder

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)
        resourceFinder = mockk()

        compileStateService = CompileStateService("models/Queue.java", "handlers/UsesQueueHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils

        QueueResourceCreator(roundEnvironment, cfDocuments, nimbusState).handleAgnosticType(elements.getTypeElement("models.Queue"))

        HttpFunctionResourceCreator(cfDocuments, nimbusState, processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesQueueHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())
        HttpFunctionResourceCreator(cfDocuments, nimbusState, processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesQueueHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        iamRoleResource = cfDocuments["dev"]!!.updateTemplate.resources.get("IamRoleUsesQueueHandlerfunc") as IamRoleResource
        usesQueueProcessor = UsesQueueProcessor(cfDocuments, messager, resourceFinder)

        toRun()
    }

    @Test
    fun correctlySetsPermissions() {
        compileStateService.compileObjects {
            setup(it) {
                every { resourceFinder.getQueueResource(any(), any(), any()) } returns QueueResource(nimbusState, "messageQueue", 10, "dev")

                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesQueueHandlerfuncFunction") as FunctionResource

                usesQueueProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesQueueHandler").enclosedElements[1], functionResource)
                val queueResource = cfDocuments["dev"]!!.updateTemplate.resources.get("SQSQueuemessageQueue")!!

                functionResource.usesClient(ClientType.Queue) shouldBe true
                functionResource.getStrEnvValue("NIMBUS_STAGE") shouldBe "dev"
                functionResource.getJsonEnvValue("NIMBUS_QUEUE_URL_ID_MESSAGEQUEUE") shouldNotBe null

                iamRoleResource.allows("sqs:SendMessage", queueResource) shouldBe true
            }
        }

        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

    @Test
    fun throwsErrorIfQueueDoesNotExist() {
        compileStateService.compileObjects {
            setup(it) {
                every { resourceFinder.getQueueResource(any(), any(), any()) } returns null

                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesQueueHandlerfunc2Function") as FunctionResource

                usesQueueProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesQueueHandler").enclosedElements[2], functionResource)

                verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
            }
        }
    }
}
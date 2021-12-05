package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.resources.NotificationTopicResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.IamRoleResource
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

class UsesNotificationTopicProcessorTest: AnnotationSpec() {

    private lateinit var usesNotificationTopicProcessor: UsesNotificationTopicProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var processingData: ProcessingData
    private lateinit var functionResource: FunctionResource
    private lateinit var iamRoleResource: IamRoleResource
    private lateinit var messager: Messager
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        processingData = ProcessingData(NimbusState())
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk()

        compileStateService = CompileStateService("models/NotificationTopic.java", "handlers/UsesNotificationTopicHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils

        NotificationTopicResourceCreator(roundEnvironment, cfDocuments, processingData.nimbusState).handleAgnosticType(elements.getTypeElement("models.NotificationTopic"))
        HttpFunctionResourceCreator(cfDocuments, processingData, mockk(relaxed = true), processingEnvironment, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesNotificationTopicHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, processingData.nimbusState))

        functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesNotificationTopicHandlerfuncFunction") as FunctionResource
        iamRoleResource = cfDocuments["dev"]!!.updateTemplate.resources.get("IamRoletionTopicHandlerfunc") as IamRoleResource
        usesNotificationTopicProcessor = UsesNotificationTopicProcessor(cfDocuments, messager, ResourceFinder(cfDocuments, processingEnvironment, processingData.nimbusState), processingData.nimbusState)

        toRun()
    }

    @Test
    fun correctlySetsPermissions() {
        compileStateService.compileObjects {
            setup(it) {
                usesNotificationTopicProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesNotificationTopicHandler").enclosedElements[1], functionResource)
                val notificationTopicResource = cfDocuments["dev"]!!.updateTemplate.resources.get("SNSTopicnotificationTopic")!!

                functionResource.usesClient(ClientType.Notification) shouldBe true
                functionResource.getStrEnvValue("NIMBUS_STAGE") shouldBe "dev"
                functionResource.getJsonEnvValue("SNS_TOPIC_ARN_NOTIFICATIONTOPIC") shouldNotBe null

                iamRoleResource.allows("sns:Subscribe", notificationTopicResource) shouldBe true
                iamRoleResource.allows("sns:Unsubscribe", notificationTopicResource) shouldBe true
                iamRoleResource.allows("sns:Publish", notificationTopicResource) shouldBe true
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

}

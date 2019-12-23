package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.mockk
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

class UsesNotificationTopicProcessorTest: AnnotationSpec() {

    private lateinit var usesNotificationTopicProcessor: UsesNotificationTopicProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var elements: Elements
    private lateinit var functionResource: FunctionResource
    private lateinit var iamRoleResource: IamRoleResource
    private lateinit var messager: Messager

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk()

        val compileState = CompileStateService("handlers/UsesNotificationTopicHandler.java")
        elements = compileState.elements

        HttpFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesNotificationTopicHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesNotificationTopicHandlerfuncFunction") as FunctionResource
        iamRoleResource = cfDocuments["dev"]!!.updateTemplate.resources.get("IamRoletionTopicHandlerfuncExecution") as IamRoleResource
        usesNotificationTopicProcessor = UsesNotificationTopicProcessor(cfDocuments, nimbusState, messager)
    }

    @Test
    fun correctlySetsPermissions() {
        usesNotificationTopicProcessor.handleUseResources(elements.getTypeElement("handlers.UsesNotificationTopicHandler").enclosedElements[1], functionResource)
        val notificationTopicResource = cfDocuments["dev"]!!.updateTemplate.resources.get("SNSTopicTest")!!

        functionResource.usesClient(ClientType.Notification) shouldBe true
        functionResource.getStrEnvValue("NIMBUS_STAGE") shouldBe "dev"
        functionResource.getJsonEnvValue("SNS_TOPIC_ARN_TEST") shouldNotBe null

        iamRoleResource.allows("sns:Subscribe", notificationTopicResource) shouldBe true
        iamRoleResource.allows("sns:Unsubscribe", notificationTopicResource) shouldBe true
        iamRoleResource.allows("sns:Publish", notificationTopicResource) shouldBe true
    }

}
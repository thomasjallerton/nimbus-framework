package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.BasicFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

class UsesBasicServerlessFunctionClientProcessorTest: AnnotationSpec() {

    private lateinit var usesBasicServerlessFunctionClientProcessor: UsesBasicServerlessFunctionClientProcessor
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

        val compileState = CompileStateService("handlers/BasicHandlers.java", "handlers/UsesBasicFunctionHandler.java")
        elements = compileState.elements

        BasicFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment).handleElement(elements.getTypeElement("handlers.BasicHandlers").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        HttpFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesBasicFunctionHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())
        HttpFunctionResourceCreator(cfDocuments, nimbusState, compileState.processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesBasicFunctionHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        iamRoleResource = cfDocuments["dev"]!!.updateTemplate.resources.get("IamRolecFunctionHandlerfunc") as IamRoleResource
        usesBasicServerlessFunctionClientProcessor = UsesBasicServerlessFunctionClientProcessor(cfDocuments, compileState.processingEnvironment, nimbusState, messager)
    }

    @Test
    fun correctlySetsPermissions() {
        val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesBasicFunctionHandlerfuncFunction") as FunctionResource

        usesBasicServerlessFunctionClientProcessor.handleUseResources(elements.getTypeElement("handlers.UsesBasicFunctionHandler").enclosedElements[1], functionResource)
        val basicFunctionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("BasicHandlersgetCurrentTimeFunction")!!

        iamRoleResource.allows("lambda:*", basicFunctionResource) shouldBe true

        functionResource.usesClient(ClientType.BasicFunction) shouldBe true
        functionResource.getStrEnvValue("NIMBUS_PROJECT_NAME") shouldBe ""
        functionResource.getStrEnvValue("FUNCTION_STAGE") shouldBe "dev"
    }

    @Test
    fun alertsIfNoCorrespondingBasicFunction() {
        val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesBasicFunctionHandlerfunc2Function") as FunctionResource

        usesBasicServerlessFunctionClientProcessor.handleUseResources(elements.getTypeElement("handlers.UsesBasicFunctionHandler").enclosedElements[2], functionResource)

        verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
    }
}
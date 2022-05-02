package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.processor.ProcessingData
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.UsesServerlessFunctionWebSocketClientProcessor
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.WebSocketFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class UsesServerlessFunctionWebSocketClientProcessorTest: AnnotationSpec() {

    private lateinit var usesServerlessFunctionWebSocketClientProcessor: UsesServerlessFunctionWebSocketClientProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var processingData: ProcessingData
    private lateinit var functionResource: FunctionResource
    private lateinit var iamRoleResource: IamRoleResource
    private lateinit var messager: Messager
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        processingData = ProcessingData(nimbusState)
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk()

        compileStateService = CompileStateService("handlers/UsesWebSocketHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils
        WebSocketFunctionResourceCreator(cfDocuments, processingData, mockk(relaxed = true), processingEnvironment, setOf(), mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesWebSocketHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, processingData))

        functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesWebSocketHandlerfuncFunction") as FunctionResource
        iamRoleResource = cfDocuments["dev"]!!.updateTemplate.resources.get("IamRoleWebSocketHandlerfunc") as IamRoleResource
        usesServerlessFunctionWebSocketClientProcessor = UsesServerlessFunctionWebSocketClientProcessor(cfDocuments, nimbusState)
        toRun()
    }

    @Test
    fun correctlySetsPermissions() {
        compileStateService.compileObjects {
            setup(it) {
                usesServerlessFunctionWebSocketClientProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesWebSocketHandler").enclosedElements[1], functionResource)
                val webSocketApi = cfDocuments["dev"]!!.updateTemplate.rootWebSocketApi!!

                functionResource.getJsonEnvValue("WEBSOCKET_ENDPOINT") shouldNotBe null

                iamRoleResource.allows("execute-api:ManageConnections", webSocketApi, "/*") shouldBe true
            }
        }
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }

}

package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.functions.WebSocketFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.resources.RelationalDatabaseResourceCreator
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
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements

class UsesServerlessFunctionWebSocketClientProcessorTest: AnnotationSpec() {

    private lateinit var usesServerlessFunctionWebSocketClientProcessor: UsesServerlessFunctionWebSocketClientProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var functionResource: FunctionResource
    private lateinit var iamRoleResource: IamRoleResource
    private lateinit var messager: Messager
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk()

        compileStateService = CompileStateService("handlers/UsesWebSocketHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils
        WebSocketFunctionResourceCreator(cfDocuments, nimbusState, processingEnvironment).handleElement(elements.getTypeElement("handlers.UsesWebSocketHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

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

                functionResource.usesClient(ClientType.WebSocket) shouldBe true
                functionResource.getJsonEnvValue("WEBSOCKET_ENDPOINT") shouldNotBe null

                iamRoleResource.allows("execute-api:ManageConnections", webSocketApi, "/*") shouldBe true
            }
        }
    }

    @AfterEach
    fun final() {
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }
}
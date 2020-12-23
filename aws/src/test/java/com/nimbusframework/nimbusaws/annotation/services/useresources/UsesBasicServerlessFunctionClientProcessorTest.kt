package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.google.testing.compile.Compilation
import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService
import com.nimbusframework.nimbusaws.annotation.services.functions.BasicFunctionResourceCreator
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.IamRoleResource
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.tools.Diagnostic

class UsesBasicServerlessFunctionClientProcessorTest: AnnotationSpec() {

    private lateinit var usesBasicServerlessFunctionClientProcessor: UsesBasicServerlessFunctionClientProcessor
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var iamRoleResource: IamRoleResource
    private lateinit var messager: Messager
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        messager = mockk(relaxed = true)

        compileStateService = CompileStateService("handlers/BasicHandlers.java", "handlers/UsesBasicFunctionHandler.java")
    }

    private fun setup(processingEnvironment: ProcessingEnvironment, toRun: () -> Unit ) {
        val elements = processingEnvironment.elementUtils
        usesBasicServerlessFunctionClientProcessor = UsesBasicServerlessFunctionClientProcessor(cfDocuments, processingEnvironment, nimbusState, messager)

        BasicFunctionResourceCreator(cfDocuments, nimbusState, processingEnvironment, mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.BasicHandlers").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        HttpFunctionResourceCreator(cfDocuments, nimbusState, processingEnvironment, mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesBasicFunctionHandler").enclosedElements[1], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())
        HttpFunctionResourceCreator(cfDocuments, nimbusState, processingEnvironment, mockk(relaxed = true)).handleElement(elements.getTypeElement("handlers.UsesBasicFunctionHandler").enclosedElements[2], FunctionEnvironmentService(cfDocuments, nimbusState), mutableListOf())

        iamRoleResource = cfDocuments["dev"]!!.updateTemplate.resources.get("IamRolecFunctionHandlerfunc") as IamRoleResource

        toRun()
    }

    @Test
    fun correctlySetsPermissions() {
        compileStateService.compileObjects {
            setup(it) {
                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesBasicFunctionHandlerfuncFunction") as FunctionResource

                usesBasicServerlessFunctionClientProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesBasicFunctionHandler").enclosedElements[1], functionResource)
                val basicFunctionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("BasicHandlersgetCurrentTimeFunction")!!

                iamRoleResource.allows("lambda:*", basicFunctionResource) shouldBe true

                functionResource.usesClient(ClientType.BasicFunction) shouldBe true
                functionResource.getStrEnvValue("NIMBUS_PROJECT_NAME") shouldBe ""
                functionResource.getStrEnvValue("FUNCTION_STAGE") shouldBe "dev"
            }
        }

    }

    @Test
    fun alertsIfNoCorrespondingBasicFunction() {
        compileStateService.compileObjects {
            setup(it) {
                val functionResource = cfDocuments["dev"]!!.updateTemplate.resources.get("UsesBasicFunctionHandlerfunc2Function") as FunctionResource

                usesBasicServerlessFunctionClientProcessor.handleUseResources(it.elementUtils.getTypeElement("handlers.UsesBasicFunctionHandler").enclosedElements[2], functionResource)

                verify { messager.printMessage(Diagnostic.Kind.ERROR, any(), any()) }
            }
        }
    }

    @AfterEach
    fun final() {
        compileStateService.status shouldBe Compilation.Status.SUCCESS
    }
}
package com.nimbusframework.nimbusaws.annotation.services

import com.google.gson.JsonObject
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationTemplate
import com.nimbusframework.nimbusaws.cloudformation.resource.Resource
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.specs.AnnotationSpec
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.Writer
import javax.annotation.processing.Filer
import javax.tools.JavaFileObject
import javax.tools.StandardLocation

class CloudFormationWriterTest : AnnotationSpec() {

    private lateinit var filer: Filer
    private lateinit var cloudFormationWriter: CloudFormationWriter

    @BeforeEach
    fun setUp() {
        filer = mockk()
        cloudFormationWriter = CloudFormationWriter(filer)
    }

    @Test
    fun canSaveValidTemplate() {
        val nimbusState = NimbusState()
        val template = CloudFormationTemplate(nimbusState, "dev")
        template.resources.addResource(DummyResource(nimbusState, "dev"))

        val file = mockk<JavaFileObject>()
        val writer = mockk<Writer>()

        every { filer.createResource(StandardLocation.SOURCE_OUTPUT, "nimbus", "test.json") } returns file
        every { file.openWriter() } returns writer
        every { writer.write(ofType(String::class)) } returns Unit
        every { writer.close() } returns Unit

        cloudFormationWriter.saveTemplate("test", template)

        verify { writer.write(template.toJson()) }
    }

    @Test
    fun doesNotSaveInvalidTemplate() {
        val nimbusState = NimbusState()
        val template = mockk<CloudFormationTemplate>()

        every { template.valid() } returns false

        cloudFormationWriter.saveTemplate("test", template)

        verify { filer wasNot called }
    }

    private class DummyResource(nimbusState: NimbusState, stage: String): Resource(nimbusState, stage) {
        override fun toCloudFormation(): JsonObject {
            return JsonObject()
        }

        override fun getName(): String {
            return "TEST"
        }

    }

}
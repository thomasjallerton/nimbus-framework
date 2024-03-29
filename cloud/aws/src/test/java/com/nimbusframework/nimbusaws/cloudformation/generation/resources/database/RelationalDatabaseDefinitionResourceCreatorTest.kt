package com.nimbusframework.nimbusaws.cloudformation.generation.resources.database

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.database.RelationalDatabaseResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.annotation.annotations.database.ParsedDatabaseConfig
import com.nimbusframework.nimbusaws.cloudformation.model.resource.database.RdsResource
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.DatabaseSize
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment


internal class RelationalDatabaseDefinitionResourceCreatorTest: AnnotationSpec() {

    private lateinit var relationalDatabaseResourceCreator: RelationalDatabaseResourceCreator
    private lateinit var roundEnvironment: RoundEnvironment
    private lateinit var cfDocuments: MutableMap<String, CloudFormationFiles>
    private lateinit var nimbusState: NimbusState
    private lateinit var compileStateService: CompileStateService

    @BeforeEach
    fun setup() {
        nimbusState = NimbusState()
        cfDocuments = mutableMapOf()
        roundEnvironment = mockk()
        compileStateService = CompileStateService("models/RelationalDatabaseModel.java", "models/RdsDatabaseModel.java")
        relationalDatabaseResourceCreator = RelationalDatabaseResourceCreator(roundEnvironment, cfDocuments, nimbusState)
    }

    @Test
    fun correctlyProcessesRelationDatabaseAnnotation() {
        compileStateService.compileObjects {
            relationalDatabaseResourceCreator.handleAgnosticType(it.elementUtils.getTypeElement("models.RelationalDatabaseModel"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 13

            val rdsResource = resources.get("RdsInstancetestRelationalDatabase") as RdsResource
            rdsResource shouldNotBe null

            val config = rdsResource.parsedDatabaseConfig
            config.awsDatabaseInstance shouldBe ParsedDatabaseConfig.toInstanceType(DatabaseSize.FREE)
            config.databaseLanguage shouldBe DatabaseLanguage.MYSQL
            config.name shouldBe "testRelationalDatabase"
            config.username shouldBe "username"
            config.password shouldBe "password"
            config.size shouldBe 30
        }
    }

    @Test
    fun correctlyProcessesRdsDatabaseAnnotation() {
        compileStateService.compileObjects {

            relationalDatabaseResourceCreator.handleSpecificType(it.elementUtils.getTypeElement("models.RdsDatabaseModel"))
            cfDocuments["dev"] shouldNotBe null

            val resources = cfDocuments["dev"]!!.updateTemplate.resources
            resources.size() shouldBe 13

            val rdsResource = resources.get("RdsInstancetestRdsDatabase") as RdsResource

            rdsResource shouldNotBe null

            val config = rdsResource.parsedDatabaseConfig
            config.awsDatabaseInstance shouldBe "micro"
            config.databaseLanguage shouldBe DatabaseLanguage.MYSQL
            config.name shouldBe "testRdsDatabase"
            config.username shouldBe "username"
            config.password shouldBe "password"
            config.size shouldBe 30
        }
    }
}

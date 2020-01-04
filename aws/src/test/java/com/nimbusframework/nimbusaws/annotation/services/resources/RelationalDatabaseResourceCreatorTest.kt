package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.CompileStateService
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.database.RdsConfiguration
import com.nimbusframework.nimbusaws.cloudformation.resource.database.RdsResource
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.DatabaseSize
import com.nimbusframework.nimbuscore.persisted.NimbusState
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.mockk
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements


internal class RelationalDatabaseResourceCreatorTest: AnnotationSpec() {

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

            val config = rdsResource.rdsConfiguration
            config.awsDatabaseInstance shouldBe RdsConfiguration.toInstanceType(DatabaseSize.FREE)
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

            val config = rdsResource.rdsConfiguration
            config.awsDatabaseInstance shouldBe "micro"
            config.databaseLanguage shouldBe DatabaseLanguage.MYSQL
            config.name shouldBe "testRdsDatabase"
            config.username shouldBe "username"
            config.password shouldBe "password"
            config.size shouldBe 30
        }
    }
}
package com.nimbusframework.nimbusaws.annotation.services.useresources

import com.microsoft.sqlserver.jdbc.SQLServerDriver
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.UsesRelationalDatabase
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.UsesRelationalDatabaseAnnotation
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class UsesRelationalDatabaseProcessor(
        private val cfDocuments: Map<String, CloudFormationFiles>,
        private val processingEnv: ProcessingEnvironment,
        nimbusState: NimbusState
): UsesResourcesProcessor(nimbusState) {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val resourceFinder = ResourceFinder(cfDocuments, processingEnv, nimbusState)

        for (usesRelationalDatabase in serverlessMethod.getAnnotationsByType(UsesRelationalDatabase::class.java)) {
            functionResource.addClient(ClientType.Database)

            for (stage in stageService.determineStages(usesRelationalDatabase.stages)) {
                if (stage == functionResource.stage) {

                    val annotation = UsesRelationalDatabaseAnnotation(usesRelationalDatabase)
                    val resource = resourceFinder.getRelationalDatabaseResource(annotation, serverlessMethod, stage)

                    if (resource != null) {
                        functionResource.addEnvVariable(resource.getName() + "_CONNECTION_URL", resource.getAttribute("Endpoint.Address"))
                        functionResource.addEnvVariable(resource.getName() + "_PASSWORD", resource.rdsConfiguration.password)
                        functionResource.addEnvVariable(resource.getName() + "_USERNAME", resource.rdsConfiguration.username)
                        functionResource.addDependsOn(resource)

                        val dependency = when(resource.rdsConfiguration.databaseLanguage) {
                            DatabaseLanguage.MYSQL -> com.mysql.cj.jdbc.Driver::class.java.canonicalName
                            DatabaseLanguage.ORACLE -> "oracle.jdbc.driver.OracleDriver"
                            DatabaseLanguage.MARIADB -> org.mariadb.jdbc.Driver::class.java.canonicalName
                            DatabaseLanguage.SQLSERVER -> SQLServerDriver::class.java.canonicalName
                            DatabaseLanguage.POSTGRESQL -> org.postgresql.Driver::class.java.canonicalName
                        }
                        functionResource.addExtraDependency(dependency)
                    }

                }
            }
        }
    }
}
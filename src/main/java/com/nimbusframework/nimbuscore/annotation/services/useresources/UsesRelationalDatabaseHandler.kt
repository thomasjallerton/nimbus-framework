package com.nimbusframework.nimbuscore.annotation.services.useresources

import com.microsoft.sqlserver.jdbc.SQLServerDriver
import com.mysql.jdbc.Driver
import com.nimbusframework.nimbuscore.annotation.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotation.annotations.database.UsesRelationalDatabase
import com.nimbusframework.nimbuscore.annotation.services.ResourceFinder
import com.nimbusframework.nimbuscore.annotation.wrappers.annotations.datamodel.UsesRelationalDatabaseAnnotation
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationDocuments
import com.nimbusframework.nimbuscore.cloudformation.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.persisted.ClientType
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class UsesRelationalDatabaseHandler(
        private val cfDocuments: Map<String, CloudFormationDocuments>,
        private val processingEnv: ProcessingEnvironment,
        private val nimbusState: NimbusState
): UsesResourcesHandler {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val resourceFinder = ResourceFinder(cfDocuments, processingEnv, nimbusState)

        for (usesRelationalDatabase in serverlessMethod.getAnnotationsByType(UsesRelationalDatabase::class.java)) {
            functionResource.addClient(ClientType.Database)

            for (stage in usesRelationalDatabase.stages) {
                if (stage == functionResource.stage) {

                    val annotation = UsesRelationalDatabaseAnnotation(usesRelationalDatabase)
                    val resource = resourceFinder.getRelationalDatabaseResource(annotation, serverlessMethod, stage)

                    if (resource != null) {
                        functionResource.addEnvVariable(resource.getName() + "_CONNECTION_URL", resource.getAttribute("Endpoint.Address"))
                        functionResource.addEnvVariable(resource.getName() + "_PASSWORD", resource.databaseConfiguration.password)
                        functionResource.addEnvVariable(resource.getName() + "_USERNAME", resource.databaseConfiguration.username)
                        functionResource.addDependsOn(resource)

                        val dependency = when(resource.databaseConfiguration.databaseLanguage) {
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
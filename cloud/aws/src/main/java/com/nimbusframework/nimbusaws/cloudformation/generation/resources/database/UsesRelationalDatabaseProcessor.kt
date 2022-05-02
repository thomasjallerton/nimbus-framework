package com.nimbusframework.nimbusaws.cloudformation.generation.resources.database

import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.UsesResourcesProcessor
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.annotations.database.UsesRelationalDatabase
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.UsesRelationalDatabaseAnnotation
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

class UsesRelationalDatabaseProcessor(
    private val resourceFinder: ResourceFinder,
    nimbusState: NimbusState
): UsesResourcesProcessor(nimbusState) {

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {

        for (usesRelationalDatabase in serverlessMethod.getAnnotationsByType(UsesRelationalDatabase::class.java)) {

            for (stage in stageService.determineStages(usesRelationalDatabase.stages)) {
                if (stage == functionResource.stage) {

                    val annotation = UsesRelationalDatabaseAnnotation(usesRelationalDatabase)
                    val resource = resourceFinder.getRelationalDatabaseResource(annotation, serverlessMethod, stage)

                    if (resource != null) {
                        functionResource.addEnvVariable(DatabaseConnectionUrlEnvironmentVariable(resource.parsedDatabaseConfig), resource.getAttribute("Endpoint.Address"))
                        functionResource.addEnvVariable(DatabasePasswordUrlEnvironmentVariable(resource.parsedDatabaseConfig), resource.parsedDatabaseConfig.password)
                        functionResource.addEnvVariable(DatabaseUsernameUrlEnvironmentVariable(resource.parsedDatabaseConfig), resource.parsedDatabaseConfig.username)
                        functionResource.addDependsOn(resource)
                    }

                }
            }
        }
    }
}

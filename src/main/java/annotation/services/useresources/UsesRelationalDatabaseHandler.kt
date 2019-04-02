package annotation.services.useresources

import annotation.annotations.database.UsesRelationalDatabase
import annotation.services.ResourceFinder
import annotation.wrappers.annotations.datamodel.UsesRelationalDatabaseAnnotation
import cloudformation.CloudFormationDocuments
import cloudformation.resource.function.FunctionResource
import persisted.NimbusState
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
            for (stage in usesRelationalDatabase.stages) {
                if (stage == functionResource.stage) {

                    val annotation = UsesRelationalDatabaseAnnotation(usesRelationalDatabase)
                    val resource = resourceFinder.getRelationalDatabaseResource(annotation, serverlessMethod, stage)

                    if (resource != null) {
                        functionResource.addEnvVariable(resource.getName() + "_CONNECTION_URL", resource.getAttribute("Endpoint.Address"))
                        functionResource.addEnvVariable(resource.getName() + "_PASSWORD", resource.databaseConfiguration.password)
                        functionResource.addEnvVariable(resource.getName() + "_USERNAME", resource.databaseConfiguration.username)
                        functionResource.addDependsOn(resource)
                    }
                }
            }
        }
    }
}
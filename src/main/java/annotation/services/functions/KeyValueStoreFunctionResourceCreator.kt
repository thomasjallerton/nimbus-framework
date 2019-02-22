package annotation.services.functions

import annotation.models.persisted.NimbusState
import annotation.models.resource.ResourceCollection
import annotation.processor.FunctionInformation
import annotation.services.FunctionEnvironmentService
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class KeyValueStoreFunctionResourceCreator(
        updateResources: ResourceCollection,
        nimbusState: NimbusState,
        processingEnv: ProcessingEnvironment
): FunctionResourceCreator(updateResources, nimbusState, processingEnv) {


    override fun handle(roundEnv: RoundEnvironment, functionEnvironmentService: FunctionEnvironmentService): List<FunctionInformation> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
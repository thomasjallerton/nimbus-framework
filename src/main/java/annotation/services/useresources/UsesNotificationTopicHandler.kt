package annotation.services.useresources

import annotation.annotations.notification.UsesNotificationTopic
import cloudformation.CloudFormationDocuments
import cloudformation.resource.function.FunctionResource
import cloudformation.resource.notification.SnsTopicResource
import persisted.NimbusState
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesNotificationTopicHandler(
        private val cfDocuments: Map<String, CloudFormationDocuments>,
        processingEnv: ProcessingEnvironment,
        private val nimbusState: NimbusState
): UsesResourcesHandler  {

    private val messager = processingEnv.messager

    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.getIamRoleResource()

        for (notificationTopic in serverlessMethod.getAnnotationsByType(UsesNotificationTopic::class.java)) {
            for (stage in notificationTopic.stages) {
                if (stage == functionResource.stage) {

                    val snsTopicResource = SnsTopicResource(notificationTopic.topic, null, nimbusState, stage)
                    val cloudFormationDocuments = cfDocuments[stage]
                    if (cloudFormationDocuments == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "No serverless function annotation found for UsesNotificationTopic", serverlessMethod)
                        return
                    }
                    cloudFormationDocuments.updateResources.addResource(snsTopicResource)

                    functionResource.addEnvVariable("SNS_TOPIC_ARN_" + notificationTopic.topic.toUpperCase(), snsTopicResource.getRef())
                    iamRoleResource.addAllowStatement("sns:Subscribe", snsTopicResource, "")
                    iamRoleResource.addAllowStatement("sns:Unsubscribe", snsTopicResource, "")
                    iamRoleResource.addAllowStatement("sns:Publish", snsTopicResource, "")
                }
            }
        }    }
}
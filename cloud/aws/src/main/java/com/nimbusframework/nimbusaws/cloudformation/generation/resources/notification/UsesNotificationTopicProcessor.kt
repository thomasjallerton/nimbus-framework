package com.nimbusframework.nimbusaws.cloudformation.generation.resources.notification

import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.UsesResourcesProcessor
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.function.FunctionResource
import com.nimbusframework.nimbuscore.annotations.notification.UsesNotificationTopic
import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.wrappers.annotations.datamodel.UsesNotificationTopicAnnotation
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class UsesNotificationTopicProcessor(
    private val messager: Messager,
    private val resourceFinder: ResourceFinder,
    nimbusState: NimbusState
): UsesResourcesProcessor(nimbusState)  {


    override fun handleUseResources(serverlessMethod: Element, functionResource: FunctionResource) {
        val iamRoleResource = functionResource.iamRoleResource

        for (notificationTopic in serverlessMethod.getAnnotationsByType(UsesNotificationTopic::class.java)) {

            for (stage in stageService.determineStages(notificationTopic.stages)) {
                if (stage == functionResource.stage) {

                    val snsTopicResource = resourceFinder.getNotificationTopicResource(UsesNotificationTopicAnnotation(notificationTopic), serverlessMethod, stage)

                    if (snsTopicResource == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find notification topic class", serverlessMethod)
                        return
                    }

                    functionResource.addEnvVariable(NotificationTopicEnvironmentVariable(snsTopicResource.annotation), snsTopicResource.getRef())
                    iamRoleResource.addAllowStatement("sns:Subscribe", snsTopicResource, "")
                    iamRoleResource.addAllowStatement("sns:Unsubscribe", snsTopicResource, "")
                    iamRoleResource.addAllowStatement("sns:Publish", snsTopicResource, "")
                }
            }
        }
    }
}

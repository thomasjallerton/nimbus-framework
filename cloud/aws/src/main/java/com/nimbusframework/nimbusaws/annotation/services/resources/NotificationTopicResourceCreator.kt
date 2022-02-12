package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.notification.SnsTopicResource
import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition
import com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinitions
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class NotificationTopicResourceCreator(
        roundEnvironment: RoundEnvironment,
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        nimbusState: NimbusState
): CloudResourceResourceCreator(
        roundEnvironment,
        cfDocuments,
        nimbusState,
        NotificationTopicDefinition::class.java,
        NotificationTopicDefinitions::class.java
) {

    override fun handleAgnosticType(type: Element) {
        val notificationTopics = type.getAnnotationsByType(NotificationTopicDefinition::class.java)

        for (notificationTopic in notificationTopics) {
            for (stage in stageService.determineStages(notificationTopic.stages)) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
                val updateResources = cloudFormationDocuments.updateTemplate.resources

                val snsTopic = SnsTopicResource(notificationTopic.topicName, nimbusState, stage)
                updateResources.addResource(snsTopic)
            }
        }
    }

}
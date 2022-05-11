package com.nimbusframework.nimbusaws.cloudformation.generation.resources.notification

import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedNotificationTopic
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.CloudResourceResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.notification.SnsTopicResource
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

                val snsTopic = SnsTopicResource(ParsedNotificationTopic(notificationTopic), nimbusState, stage)
                updateResources.addSnsResource(snsTopic)
            }
        }
    }

}

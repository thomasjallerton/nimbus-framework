package com.nimbusframework.nimbusaws.annotation.services.resources

import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.resource.queue.QueueResource
import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition
import com.nimbusframework.nimbuscore.annotations.queue.QueueDefinitions
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class QueueResourceCreator(
        roundEnvironment: RoundEnvironment,
        cfDocuments: MutableMap<String, CloudFormationFiles>,
        nimbusState: NimbusState
): CloudResourceResourceCreator(
        roundEnvironment,
        cfDocuments,
        nimbusState,
        QueueDefinition::class.java,
        QueueDefinitions::class.java
) {

    override fun handleAgnosticType(type: Element) {
        val queues = type.getAnnotationsByType(QueueDefinition::class.java)

        for (queue in queues) {
            for (stage in stageService.determineStages(queue.stages)) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
                val updateResources = cloudFormationDocuments.updateTemplate.resources

                val sqsQueue = QueueResource(nimbusState, queue.queueId, 30, stage)
                updateResources.addResource(sqsQueue)
            }
        }
    }

}
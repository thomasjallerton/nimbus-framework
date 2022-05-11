package com.nimbusframework.nimbusaws.cloudformation.generation.resources.queue

import com.nimbusframework.nimbusaws.annotation.annotations.parsed.ParsedQueueDefinition
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.CloudResourceResourceCreator
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles
import com.nimbusframework.nimbusaws.cloudformation.model.resource.queue.QueueResource
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
        QueueDefinitions::class.java,
) {

    override fun handleAgnosticType(type: Element) {
        val queues = type.getAnnotationsByType(QueueDefinition::class.java)

        for (queueAnnotation in queues) {
            val queue = ParsedQueueDefinition(queueAnnotation)
            for (stage in stageService.determineStages(queue.stages)) {
                val cloudFormationDocuments = cfDocuments.getOrPut(stage) { CloudFormationFiles(nimbusState, stage) }
                val updateResources = cloudFormationDocuments.updateTemplate.resources

                val sqsQueue = QueueResource(queue, nimbusState, stage)
                updateResources.addQueue(sqsQueue)
            }
        }
    }

}

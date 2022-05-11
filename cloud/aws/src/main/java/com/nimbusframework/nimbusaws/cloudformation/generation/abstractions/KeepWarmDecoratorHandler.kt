package com.nimbusframework.nimbusaws.cloudformation.generation.abstractions

import com.nimbusframework.nimbusaws.annotation.processor.FunctionInformation
import com.nimbusframework.nimbuscore.annotations.function.decorator.KeepWarm
import com.nimbusframework.nimbuscore.persisted.NimbusState
import javax.lang.model.element.Element

class KeepWarmDecoratorHandler(
    private val nimbusState: NimbusState,
    private val functionEnvironmentService: FunctionEnvironmentService
) : FunctionDecoratorHandler {

    private val KEEP_WARM_CRON = "rate(10 minutes)"

    override fun handleDecorator(type: Element, functionInformation: List<FunctionInformation>) {
        if (type.getAnnotation(KeepWarm::class.java) != null) {
            functionInformation
                .filter { it.canBeKeptWarm }
                .forEach {
                    functionEnvironmentService.newCronTrigger(
                        KEEP_WARM_CRON,
                        it.resource
                    )
                }
        } else {
            functionInformation
                .filter { nimbusState.keepWarmStages.contains(it.resource.stage) }
                .filter { it.canBeKeptWarm }
                .forEach {
                    functionEnvironmentService.newCronTrigger(
                        KEEP_WARM_CRON,
                        it.resource
                    )
                }
        }
    }

}

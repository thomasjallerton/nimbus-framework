package com.nimbusframework.nimbuscore.wrappers.annotations.datamodel

import com.nimbusframework.nimbuscore.annotations.queue.UsesQueue

class UsesQueueFunctionAnnotation(private val usesQueue: UsesQueue): DataModelAnnotation() {

    override val stages = usesQueue.stages

    override fun internalDataModel(): Class<out Any> {
        return usesQueue.queue.java
    }

}

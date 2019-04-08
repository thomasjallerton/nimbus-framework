package com.nimbusframework.nimbuscore.cloudformation.resource.http

import com.nimbusframework.nimbuscore.persisted.NimbusState
import com.nimbusframework.nimbuscore.cloudformation.resource.Resource
import com.google.gson.JsonObject

abstract class AbstractRestResource (
        nimbusState: NimbusState,
        stage: String
): Resource(nimbusState, stage) {

    abstract fun getId(): JsonObject

    abstract fun getRootId(): JsonObject

    abstract fun getPath(): String
}
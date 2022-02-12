package com.nimbusframework.nimbuslocal.deployment.websocket

import com.fasterxml.jackson.databind.node.ObjectNode
import com.nimbusframework.nimbuscore.clients.JacksonClient
import com.nimbusframework.nimbuscore.eventabstractions.RequestContext


data class WebSocketRequest(
        var body: String = "null",
        var queryStringParams: Map<String, String> = mapOf(),
        var headers: Map<String, String> = mapOf(),
        var requestContext: RequestContext = RequestContext()
) {
    fun getTopic(): String {
        val tree = JacksonClient.readTree(body)

        val topic = tree["topic"]
        if (topic.isTextual) return topic.asText()

        throw MissingTopicException(body)
    }

    fun setBodyWithTopic(obj: Any, topic: String) {
        val node: ObjectNode = JacksonClient.valueToTree(obj)
        node.put("topic", topic)

        body = JacksonClient.writeValueAsString(node)
    }

    constructor(body: String,
                queryStringParams: Map<String, String>,
                headers: Map<String, String>): this(body, queryStringParams, headers, RequestContext())
}

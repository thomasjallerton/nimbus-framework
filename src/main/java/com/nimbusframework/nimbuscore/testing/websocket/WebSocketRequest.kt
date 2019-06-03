package com.nimbusframework.nimbuscore.testing.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nimbusframework.nimbuscore.wrappers.websocket.models.RequestContext


data class WebSocketRequest(
        var body: String = "null",
        var queryStringParams: Map<String, String> = mapOf(),
        var headers: Map<String, String> = mapOf(),
        var requestContext: RequestContext = RequestContext()
) {
    fun getTopic(): String {
        val objectMapper = ObjectMapper()
        val tree = objectMapper.readTree(body)

        val topic = tree["topic"]
        if (topic.isTextual) return topic.asText()

        throw MissingTopicException(body)
    }

    fun setBodyWithTopic(obj: Any, topic: String) {
        val objectMapper = ObjectMapper()
        val node: ObjectNode = objectMapper.valueToTree(obj)
        node.put("topic", topic)

        body = objectMapper.writeValueAsString(node)
    }

    constructor(body: String,
                queryStringParams: Map<String, String>,
                headers: Map<String, String>): this(body, queryStringParams, headers, RequestContext())
}
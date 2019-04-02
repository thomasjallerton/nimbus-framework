package testing.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import wrappers.websocket.models.RequestContext


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

    constructor(body: String,
                queryStringParams: Map<String, String>,
                headers: Map<String, String>): this(body, queryStringParams, headers, RequestContext())
}
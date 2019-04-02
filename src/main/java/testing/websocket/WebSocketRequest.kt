package testing.websocket

import com.fasterxml.jackson.databind.ObjectMapper


data class WebSocketRequest(
        var body: String = "null",
        var queryStringParams: Map<String, String> = mapOf(),
        var headers: Map<String, String> = mapOf()
) {
    fun getTopic(): String {
        val objectMapper = ObjectMapper()
        val tree = objectMapper.readTree(body)

        val topic = tree["topic"]
        if (topic.isTextual) return topic.asText()

        throw MissingTopicException(body)
    }
}
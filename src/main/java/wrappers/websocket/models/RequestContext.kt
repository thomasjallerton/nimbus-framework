package wrappers.websocket.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class RequestContext(
    val eventType: String = "",
    val connectionId: String = ""
)
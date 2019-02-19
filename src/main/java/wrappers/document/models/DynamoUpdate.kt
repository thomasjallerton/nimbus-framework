package wrappers.document.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DynamoUpdate (
    @JsonProperty(value = "NewImage")
    val newImage: Map<String, Map<String, String>>? = null,
    @JsonProperty(value = "OldImage")
    val oldImage: Map<String, Map<String, String>>? = null
)
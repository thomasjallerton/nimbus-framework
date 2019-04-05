package wrappers.store.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class StoreUpdateDetails (
    @JsonProperty(value = "NewImage")
    val newImage: Map<String, Map<String, String>>? = null,
    @JsonProperty(value = "OldImage")
    val oldImage: Map<String, Map<String, String>>? = null
)
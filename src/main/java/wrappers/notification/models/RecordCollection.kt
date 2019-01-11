package wrappers.notification.models

import com.fasterxml.jackson.annotation.JsonProperty

class RecordCollection {
    @JsonProperty(value = "Records")
    val records: List<Record>? = null
}
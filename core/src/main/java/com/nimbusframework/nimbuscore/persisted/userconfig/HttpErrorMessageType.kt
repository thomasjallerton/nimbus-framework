package com.nimbusframework.nimbuscore.persisted.userconfig

enum class HttpErrorMessageType {
    /** Error messages are not sent to the client */
    NONE,
    /** Error messages are encoded as plain text in the response body */
    PLAIN_TEXT,
    /** Error messages are encoded as JSON in the response body, e.g. {"message": "..."} */
    APPLICATION_JSON,
    /** Error messages are sent in the "Nimbus-Error-Message" header, with an empty body */
    HEADER
}

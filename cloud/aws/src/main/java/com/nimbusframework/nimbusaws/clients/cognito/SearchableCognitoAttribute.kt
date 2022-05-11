package com.nimbusframework.nimbusaws.clients.cognito

enum class SearchableCognitoAttribute(val searchTerm: String) {
    USERNAME("username"),
    EMAIL("email"),
    PHONE_NUMBER("phone_number"),
    NAME("name"),
    GIVEN_NAME("given_name"),
    FAMILY_NAME("family_name"),
    PREFERRED_USERNAME("preferred_username"),
    STATUS("cognito:user_status"),
    ENABLED("status"),
    SUB("sub")
}

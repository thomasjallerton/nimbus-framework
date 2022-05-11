package com.nimbusframework.nimbusaws.clients.cognito

enum class SearchType(val formatted: String) {
    EQUALS("="),
    PREFIX("^=")
}

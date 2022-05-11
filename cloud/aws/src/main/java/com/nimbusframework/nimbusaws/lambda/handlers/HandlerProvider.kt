package com.nimbusframework.nimbusaws.lambda.handlers

data class HandlerProvider(
    private val classFilePath: String,
    private val handler: String
): HandlerInformationProvider {

    override fun getClassFilePath(): String {
        return classFilePath
    }

    override fun getHandler(): String {
        return handler
    }
}

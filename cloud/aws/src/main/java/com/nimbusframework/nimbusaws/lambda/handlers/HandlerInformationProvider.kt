package com.nimbusframework.nimbusaws.lambda.handlers

interface HandlerInformationProvider {

    fun getClassFilePath(): String

    fun getHandler(): String

}

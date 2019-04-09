package com.nimbusframework.nimbuscore.testing.services.resource

interface LocalCreateResourcesHandler {

    fun createResource(clazz: Class<out Any>)

}
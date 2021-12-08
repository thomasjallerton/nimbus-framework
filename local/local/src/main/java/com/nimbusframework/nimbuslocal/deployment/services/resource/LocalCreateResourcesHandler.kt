package com.nimbusframework.nimbuslocal.deployment.services.resource

interface LocalCreateResourcesHandler {

    fun createResource(clazz: Class<out Any>)

}
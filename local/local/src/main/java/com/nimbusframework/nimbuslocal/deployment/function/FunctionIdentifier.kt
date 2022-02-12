package com.nimbusframework.nimbuslocal.deployment.function

data class FunctionIdentifier(val className: String, val methodName: String) {
    constructor(clazz: Class<*>, methodName: String): this(clazz.canonicalName, methodName)
}

package com.nimbusframework.nimbuscore.clients.dynamo

class MismatchedTypeException(expectedClass: Any, fieldName: String):
        Exception("Input not expected Type. $fieldName expects ${expectedClass.javaClass.simpleName}, but dynamo contained something different.")
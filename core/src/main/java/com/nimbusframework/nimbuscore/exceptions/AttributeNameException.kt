package com.nimbusframework.nimbuscore.exceptions

class AttributeNameException(val attributeName: String): Exception("Invalid attribute $attributeName, overlaps with primary key")
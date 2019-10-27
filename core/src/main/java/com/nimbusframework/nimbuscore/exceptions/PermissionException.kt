package com.nimbusframework.nimbuscore.exceptions

class PermissionException(clientName: String): Exception("Invalid permissions for $clientName, does the function have the correct @UsesResource annotation")
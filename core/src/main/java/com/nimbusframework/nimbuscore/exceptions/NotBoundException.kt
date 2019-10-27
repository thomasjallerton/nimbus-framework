package com.nimbusframework.nimbuscore.exceptions

import java.lang.Exception

class NotBoundException(targetBinding: Class<*>): Exception("Could not find binding for $targetBinding") {
}
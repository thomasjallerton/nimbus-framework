package com.nimbusframework.nimbuslocal.deployment.websocket

import java.lang.Exception

class MissingTopicException(body: String): Exception("Missing topic field in $body")
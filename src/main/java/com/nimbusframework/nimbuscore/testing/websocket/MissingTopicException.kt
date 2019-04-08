package com.nimbusframework.nimbuscore.testing.websocket

import java.lang.Exception

class MissingTopicException(body: String): Exception("Missing topic field in $body")
package com.nimbusframework.nimbuscore.clients.queue

class InvalidQueueUrlException: Exception("Not a valid queue url. Have you set up the @UsesQueue com.nimbusframework.nimbuscore.annotation correctly?")
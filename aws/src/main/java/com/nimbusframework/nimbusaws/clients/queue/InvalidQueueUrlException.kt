package com.nimbusframework.nimbusaws.clients.queue

class InvalidQueueUrlException: Exception("Not a valid queue url. Have you set up the @UsesQueue com.nimbusframework.nimbuscore.annotation correctly?")
package com.nimbusframework.nimbusaws.clients.notification

import java.lang.Exception

class InvalidTopicException: Exception("Could not find SNS topic ARN, have you used the @UsesNotificationTopic com.nimbusframework.nimbuscore.annotation?")
package com.nimbusframework.nimbusaws.wrappers.customRuntimeEntry

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger

class CustomContext(private val requestId: String): Context {
    override fun getAwsRequestId(): String {
        return requestId
    }

    override fun getLogGroupName(): String {
        return ""
    }

    override fun getLogStreamName(): String {
        return ""
    }

    override fun getFunctionName(): String {
        return ""
    }

    override fun getFunctionVersion(): String {
        return ""
    }

    override fun getInvokedFunctionArn(): String {
        return ""
    }

    override fun getIdentity(): CognitoIdentity? {
        return null
    }

    override fun getClientContext(): ClientContext? {
        return null
    }

    override fun getRemainingTimeInMillis(): Int {
        return 0
    }

    override fun getMemoryLimitInMB(): Int {
        return 0
    }

    override fun getLogger(): LambdaLogger? {
        return null
    }
}

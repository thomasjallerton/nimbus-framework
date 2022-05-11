package com.nimbusframework.nimbusaws.annotation.annotations.cognito

import com.nimbusframework.nimbuscore.exceptions.InvalidStageException

object CognitoArnAnnotationService {

    fun getArn(clazz: Class<*>, stage: String): String {
        val cognitoAnnotations = clazz.getDeclaredAnnotationsByType(ExistingCognitoUserPool::class.java)
        // Attempt to find specific annotation for this stage. If none exist then there is one annotation that has no stage (so uses the defaults)
        for (cognitoUserPool in cognitoAnnotations) {
            if (cognitoUserPool.stages.contains(stage)) {
                return cognitoUserPool.arn
            }
        }
        val queue = cognitoAnnotations.firstOrNull { it.stages.isEmpty() } ?: throw InvalidStageException()
        return queue.arn
    }


}

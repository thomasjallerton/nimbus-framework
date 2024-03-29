package com.nimbusframework.nimbusawslocal.aws

import com.nimbusframework.nimbusaws.clients.AwsClientBinder
import com.nimbusframework.nimbusawslocal.aws.apigateway.LambdaAuthorizerFunctionCreator
import com.nimbusframework.nimbusawslocal.aws.apigateway.LocalApiGatewayResourceCreator
import com.nimbusframework.nimbusawslocal.aws.cognito.LocalCognito
import com.nimbusframework.nimbusawslocal.aws.cognito.LocalCognitoResourceCreator
import com.nimbusframework.nimbusawslocal.aws.cognito.LocalUsesCognitoHandler
import com.nimbusframework.nimbusawslocal.aws.secretsmanager.LocalSecrets
import com.nimbusframework.nimbusawslocal.aws.secretsmanager.LocalUsesSecretManagerHandler
import com.nimbusframework.nimbusawslocal.deployment.services.resource.LocalDynamoDbDocumentStoreCreator
import com.nimbusframework.nimbusawslocal.deployment.services.resource.LocalDynamoDbKeyValueStoreCreator
import com.nimbusframework.nimbuslocal.deployment.CloudSpecificLocalDeployment
import com.nimbusframework.nimbuslocal.deployment.services.LocalResourceHolder
import com.nimbusframework.nimbuslocal.deployment.services.StageService
import com.nimbusframework.nimbuslocal.deployment.services.function.LocalFunctionHandler
import com.nimbusframework.nimbuslocal.deployment.services.resource.LocalCreateResourcesHandler
import com.nimbusframework.nimbuslocal.deployment.services.usesresources.LocalUsesResourcesHandler

class AwsSpecificLocalDeployment private constructor(): CloudSpecificLocalDeployment {

    init {
        AwsClientBinder.setInternalBuilder(InternalAwsLocalClientBuilder)
    }

    private val resourceHolder = LocalAwsResourceHolder()

    override fun getLocalCreateResourcesHandlers(localResourceHolder: LocalResourceHolder, stageService: StageService): List<LocalCreateResourcesHandler> {
        return listOf(
            LocalDynamoDbDocumentStoreCreator(localResourceHolder, stageService),
            LocalDynamoDbKeyValueStoreCreator(localResourceHolder, stageService),
            LocalCognitoResourceCreator(stageService, resourceHolder),
            LocalApiGatewayResourceCreator(stageService, resourceHolder, localResourceHolder)
        )
    }

    override fun getLocalFunctionHandlers(localResourceHolder: LocalResourceHolder, stageService: StageService): List<LocalFunctionHandler> {
        return listOf(
            LambdaAuthorizerFunctionCreator(localResourceHolder)
        )
    }

    override fun getLocalUsesResourcesHandlers(localResourceHolder: LocalResourceHolder, stageService: StageService): List<LocalUsesResourcesHandler> {
        return listOf(
            LocalUsesCognitoHandler(localResourceHolder, stageService),
            LocalUsesSecretManagerHandler(localResourceHolder, stageService)
        )
    }

    fun getUserPool(userPool: Class<*>): LocalCognito {
        return resourceHolder.cognitoUserPools[userPool] ?: throw IllegalArgumentException("User pool ${userPool.name} not found")
    }

    fun getLocalSecrets(): LocalSecrets {
        return resourceHolder.localSecrets
    }

    companion object {

        private var instance: AwsSpecificLocalDeployment? = null

        fun newInstance(): AwsSpecificLocalDeployment {
            instance = AwsSpecificLocalDeployment()
            return instance!!
        }

        fun currentInstance(): AwsSpecificLocalDeployment {
            return instance ?: throw IllegalStateException("No existing instance")
        }

    }

}

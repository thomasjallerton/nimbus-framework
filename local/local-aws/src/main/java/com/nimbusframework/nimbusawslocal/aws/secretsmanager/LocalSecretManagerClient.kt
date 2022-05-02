package com.nimbusframework.nimbusawslocal.aws.secretsmanager

import com.nimbusframework.nimbusaws.clients.secretmanager.SecretManagerClient
import com.nimbusframework.nimbusawslocal.aws.AwsPermissionTypes
import com.nimbusframework.nimbuscore.exceptions.PermissionException
import com.nimbusframework.nimbuscore.permissions.PermissionType
import com.nimbusframework.nimbuslocal.clients.LocalClient

class LocalSecretManagerClient(
    private val localSecrets: LocalSecrets
): SecretManagerClient, LocalClient(AwsPermissionTypes.SECRETS_MANAGER) {

    override val clientName: String = SecretManagerClient::class.java.simpleName

    override fun getSecret(secretName: String): String {
        val secretData = localSecrets.getSecret(secretName) ?: error("Secret $secretName does not exist")
        if (!checkPermissions(AwsPermissionTypes.SECRETS_MANAGER, secretData.arn)) {
            throw PermissionException(clientName)
        }
        return secretData.value
    }

    override fun canUse(permissionType: PermissionType): Boolean {
        return checkPermissions(permissionType, "arn")
    }

}

package com.nimbusframework.nimbusawslocal.aws.cognito

import com.nimbusframework.nimbusaws.clients.cognito.CognitoClient
import com.nimbusframework.nimbusaws.clients.cognito.CognitoUser
import com.nimbusframework.nimbusaws.clients.cognito.SearchType
import com.nimbusframework.nimbusaws.clients.cognito.SearchableCognitoAttribute
import com.nimbusframework.nimbusawslocal.aws.AwsPermissionTypes
import com.nimbusframework.nimbuscore.permissions.PermissionType
import com.nimbusframework.nimbuslocal.clients.LocalClient

class LocalCognitoClient(
    private val localCognito: LocalCognito
) : CognitoClient, LocalClient(AwsPermissionTypes.COGNITO) {

    override val clientName: String = CognitoClient::class.java.simpleName

    override fun canUse(permissionType: PermissionType): Boolean {
        return checkPermissions(permissionType, localCognito.arn)
    }

    fun addUser(accessToken: String, user: CognitoUser) {
        checkClientUse()
        localCognito.addUser(accessToken, user)
    }

    override fun getUser(accessToken: String): CognitoUser? {
        checkClientUse()
        return localCognito.getUser(accessToken)
    }

    override fun listGroupsForUserAsAdmin(username: String): List<String> {
        return localCognito.getGroupsForUser(username)
    }

    override fun searchUsers(
        filterAttribute: SearchableCognitoAttribute,
        value: String,
        searchType: SearchType
    ): List<CognitoUser> {
        checkClientUse()
        return localCognito.searchUsers(filterAttribute, value, searchType)
    }

    override fun addUserToGroupAsAdmin(username: String, groupName: String) {
        checkClientUse(AwsPermissionTypes.COGNITO_ADMIN)
        return localCognito.addUserToGroupAsAdmin(username, groupName)
    }

    override fun removeUserFromGroupAsAdmin(username: String, groupName: String) {
        checkClientUse(AwsPermissionTypes.COGNITO_ADMIN)
        return localCognito.removeUserFromGroupAsAdmin(username, groupName)
    }

    override fun setAttributeAsAdmin(username: String, attribute: String, value: String) {
        localCognito.setAttribute(username, attribute, value)
    }

}

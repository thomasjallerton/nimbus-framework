package com.nimbusframework.nimbusaws.clients.cognito

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersInGroupRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersInGroupResponse

class AwsCognitoClient(
    private val userPoolId: String,
    private val cognitoClient: CognitoIdentityProviderClient
) : CognitoClient {

    override fun getUser(accessToken: String): CognitoUser {
        val response = cognitoClient.getUser { it.accessToken(accessToken) }
        return CognitoUser(
            response.username(),
            response.userAttributes().associate { Pair(it.name(), it.value()) }
        )
    }

    override fun listGroupsForUserAsAdmin(username: String): List<String> {
        var currentResponse: AdminListGroupsForUserResponse? = null
        var nextToken: String? = null
        val result = mutableListOf<String>()
        while (currentResponse == null || currentResponse.hasGroups()) {
            currentResponse = cognitoClient.adminListGroupsForUser(
                AdminListGroupsForUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .nextToken(nextToken)
                    .limit(50)
                    .build()
            )
            nextToken = currentResponse!!.nextToken()
            result.addAll(currentResponse.groups().map { it.groupName() })
            if (nextToken == null) {
                break
            }
        }
        return result
    }

    override fun listUsersInGroup(groupName: String): List<CognitoUser> {
        var currentResponse: ListUsersInGroupResponse? = null
        var nextToken: String? = null
        val baseRequest = ListUsersInGroupRequest.builder()
            .userPoolId(userPoolId)
            .groupName(groupName)
            .limit(50)

        val result = mutableListOf<CognitoUser>()
        while (currentResponse == null || currentResponse.hasUsers()) {
            currentResponse = cognitoClient.listUsersInGroup(
                baseRequest
                    .nextToken(nextToken)
                    .build()
            )
            nextToken = currentResponse!!.nextToken()
            result.addAll(currentResponse.users().map { user -> CognitoUser(
                user.username(),
                user.attributes().associate { Pair(it.name(), it.value()) }
            ) })
            if (nextToken == null) {
                break
            }
        }
        return result
    }

    override fun searchUsers(
        filterAttribute: SearchableCognitoAttribute,
        value: String,
        searchType: SearchType
    ): List<CognitoUser> {
        val searchTerm = "${filterAttribute.searchTerm} ${searchType.formatted} \"$value\""
        val response = cognitoClient.listUsers {
            it
                .userPoolId(userPoolId)
                .filter(searchTerm)
        }
        return response.users().map { user ->
            CognitoUser(
                user.username(),
                user.attributes().associate { Pair(it.name(), it.value()) }
            )
        }
    }

    override fun addUserToGroupAsAdmin(username: String, groupName: String) {
        cognitoClient.adminAddUserToGroup {
            it
                .userPoolId(userPoolId)
                .groupName(groupName)
                .username(username)
        }
    }

    override fun removeUserFromGroupAsAdmin(username: String, groupName: String) {
        cognitoClient.adminRemoveUserFromGroup {
            it
                .userPoolId(userPoolId)
                .groupName(groupName)
                .username(username)
        }
    }

    override fun setAttributeAsAdmin(username: String, attribute: String, value: String) {
        cognitoClient.adminUpdateUserAttributes {
            it
                .userPoolId(userPoolId)
                .username(username)
                .userAttributes({ builder ->
                    builder
                        .name(attribute)
                        .value(value)
                })
        }
    }

}

package com.nimbusframework.nimbusaws.clients.cognito

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminListGroupsForUserResponse

class AwsCognitoClient(
    private val userPoolId: String,
    private val cognitoClient: CognitoIdentityProviderClient
): CognitoClient {

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
            currentResponse = cognitoClient.adminListGroupsForUser {
                it
                    .userPoolId(userPoolId)
                    .username(username)
                    .nextToken(nextToken)
                    .limit(10)
            }
            nextToken = currentResponse!!.nextToken()
            result.addAll(currentResponse.groups().map { it.groupName() })
        }
        return result
    }

    override fun searchUsers(filterAttribute: SearchableCognitoAttribute, value: String, searchType: SearchType): List<CognitoUser> {
        val response = cognitoClient.listUsers { it.filter("${filterAttribute.searchTerm} ${searchType.formatted} \\\"$value\\\"") }
        return response.users().map { user ->
            CognitoUser(
                user.username(),
                user.attributes().associate { Pair(it.name(), it.value()) }
            )
        }
    }

    override fun addUserToGroupAsAdmin(username: String, groupName: String) {
        cognitoClient.adminAddUserToGroup { it
            .userPoolId(userPoolId)
            .groupName(groupName)
            .username(username)
        }
    }

    override fun removeUserFromGroupAsAdmin(username: String, groupName: String) {
        cognitoClient.adminRemoveUserFromGroup { it
            .userPoolId(userPoolId)
            .groupName(groupName)
            .username(username)
        }
    }

    override fun setAttributeAsAdmin(username: String, attribute: String, value: String) {
        cognitoClient.adminUpdateUserAttributes { it
            .userPoolId(userPoolId)
            .username(username)
            .userAttributes({ builder -> builder
                .value(attribute)
                .name(value)
            })
        }
    }

}

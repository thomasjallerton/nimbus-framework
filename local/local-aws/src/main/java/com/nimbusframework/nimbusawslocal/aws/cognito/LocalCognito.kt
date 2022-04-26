package com.nimbusframework.nimbusawslocal.aws.cognito

import com.nimbusframework.nimbusaws.clients.cognito.CognitoUser
import com.nimbusframework.nimbusaws.clients.cognito.SearchType
import com.nimbusframework.nimbusaws.clients.cognito.SearchableCognitoAttribute

class LocalCognito(
    val arn: String
) {

    private val usersByAccessToken = mutableMapOf<String, CognitoUser>()
    private val usersByUserName = mutableMapOf<String, Pair<CognitoUser, String>>()

    private val userGroups = mutableMapOf<String, MutableSet<String>>()

    fun addUser(accessToken: String, user: CognitoUser) {
        usersByUserName[user.username] = Pair(user, accessToken)
        usersByAccessToken[accessToken] = user
    }

    fun getUser(accessToken: String): CognitoUser? {
        return usersByAccessToken[accessToken]
    }

    fun searchUsers(
        filterAttribute: SearchableCognitoAttribute,
        value: String,
        searchType: SearchType
    ): List<CognitoUser> {
        return usersByAccessToken.values.filter {
            if (searchType == SearchType.EQUALS) {
                it.attributes[filterAttribute.searchTerm] == value
            } else {
                it.attributes[filterAttribute.searchTerm]?.startsWith(value) ?: false
            }
        }
    }

    fun addUserToGroupAsAdmin(username: String, groupName: String) {
        if (!usersByUserName.containsKey(username)) {
            throw IllegalArgumentException("User does not exist")
        }
        userGroups.getOrPut(groupName) { mutableSetOf() }.add(username)
    }

    fun getGroupsForUser(username: String): List<String> {
        if (!usersByUserName.containsKey(username)) {
            throw IllegalArgumentException("User does not exist")
        }
        return userGroups.filter { it.value.contains(username) }.map { it.key }
    }

    fun removeUserFromGroupAsAdmin(username: String, groupName: String) {
        if (!usersByUserName.containsKey(username)) {
            throw IllegalArgumentException("User does not exist")
        }
        if (!userGroups.containsKey(groupName)) {
            throw IllegalArgumentException("Group does not exist")
        }
        if (!userGroups[groupName]!!.contains(username)) {
            throw IllegalArgumentException("User does not exist in group")
        }
        userGroups[groupName]!!.remove(username)
    }

    fun setAttribute(username: String, attribute: String, value: String) {
        if (!usersByUserName.containsKey(username)) {
            throw IllegalArgumentException("User does not exist")
        }
        val oldUser = usersByUserName[username]!!
        val newAttributes = oldUser.first.attributes.toMutableMap()
        newAttributes[attribute] = value
        val newUser = CognitoUser(
            username,
            newAttributes.toMap()
        )

        usersByUserName[username] = Pair(newUser, oldUser.second)
        usersByAccessToken[oldUser.second] = newUser
    }

}

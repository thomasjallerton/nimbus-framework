package com.nimbusframework.nimbusaws.clients.cognito

interface CognitoClient {

    fun getUser(accessToken: String): CognitoUser?

    fun listGroupsForUserAsAdmin(username: String): List<String>

    fun searchUsers(filterAttribute: SearchableCognitoAttribute, value: String, searchType: SearchType): List<CognitoUser>

    fun searchUsers(filterAttribute: SearchableCognitoAttribute, value: String): List<CognitoUser> = searchUsers(filterAttribute, value, SearchType.EQUALS)

    fun addUserToGroupAsAdmin(username: String, groupName: String)

    fun removeUserFromGroupAsAdmin(username: String, groupName: String)

    fun setAttributeAsAdmin(username: String, attribute: String, value: String)

}

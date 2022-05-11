package com.nimbusframework.nimbusaws.clients.cognito

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.*
import java.util.function.Consumer

class AwsCognitoClientTest : AnnotationSpec() {

    private lateinit var underTest: AwsCognitoClient
    private lateinit var cognitoClient: CognitoIdentityProviderClient

    private val userPoolId = "USER_POOL_ID"

    @BeforeEach
    fun setup() {
        cognitoClient = mockk(relaxed = true)
        underTest = AwsCognitoClient(userPoolId, cognitoClient)
    }

    @Test
    fun canGetUserByAccessToken() {
        val getUserResponse = GetUserResponse.builder().username("test")
            .userAttributes(AttributeType.builder().name("key").value("value").build())
            .build()
        val sendMessageRequestBuilder = slot<Consumer<GetUserRequest.Builder>>()
        every { cognitoClient.getUser(capture(sendMessageRequestBuilder)) } returns getUserResponse

        val result = underTest.getUser("ACCESS_TOKEN")

        val builder = GetUserRequest.builder()
        sendMessageRequestBuilder.captured.accept(builder)

        val built = builder.build()
        built.accessToken() shouldBe "ACCESS_TOKEN"

        result.username shouldBe "test"
        result.attributes shouldContainExactly mapOf(Pair("key", "value"))
    }

    @Test
    fun canListGroupsForUserAsAdmin() {
        val adminListGroupsForUserResponse1 = AdminListGroupsForUserResponse.builder()
            .groups(
                GroupType.builder().groupName("group1").build(),
                GroupType.builder().groupName("group2").build()
            )
            .nextToken("NEXT_TOKEN")
            .build()
        val adminListGroupsForUserResponse2 = AdminListGroupsForUserResponse.builder()
            .groups(
                GroupType.builder().groupName("group3").build(),
                GroupType.builder().groupName("group4").build()
            )
            .nextToken("NEXT_TOKEN_2")
            .build()
        val adminListGroupsForUserResponse3 = AdminListGroupsForUserResponse.builder().build()

        val adminListGroupsConsumer = mutableListOf<AdminListGroupsForUserRequest>()
        every { cognitoClient.adminListGroupsForUser(capture(adminListGroupsConsumer)) } returnsMany listOf(
            adminListGroupsForUserResponse1,
            adminListGroupsForUserResponse2,
            adminListGroupsForUserResponse3
        )

        val result = underTest.listGroupsForUserAsAdmin("USERNAME")
        result shouldContainExactlyInAnyOrder listOf("group1", "group2", "group3", "group4")

        // First time calls with no token
        val built1 = adminListGroupsConsumer[0]
        built1.username() shouldBe "USERNAME"
        built1.limit() shouldBe 50
        built1.nextToken() shouldBe null
        built1.userPoolId() shouldBe userPoolId

        // Second time calls with a token
        val built2 = adminListGroupsConsumer[1]
        built2.username() shouldBe "USERNAME"
        built2.limit() shouldBe 50
        built2.nextToken() shouldBe "NEXT_TOKEN"
        built2.userPoolId() shouldBe userPoolId

        // Third time calls with a token
        val built3 = adminListGroupsConsumer[2]
        built3.username() shouldBe "USERNAME"
        built3.limit() shouldBe 50
        built3.nextToken() shouldBe "NEXT_TOKEN_2"
        built3.userPoolId() shouldBe userPoolId

        adminListGroupsConsumer shouldHaveSize 3
    }

    @Test
    fun canListGroupsForUserAsAdminWhenNoPagingNeeded() {
        val adminListGroupsForUserResponse1 = AdminListGroupsForUserResponse.builder()
            .groups(
                GroupType.builder().groupName("group1").build(),
                GroupType.builder().groupName("group2").build()
            )
            .nextToken(null)
            .build()

        val adminListGroupsConsumer = mutableListOf<AdminListGroupsForUserRequest>()
        every { cognitoClient.adminListGroupsForUser(capture(adminListGroupsConsumer)) } returnsMany listOf(
            adminListGroupsForUserResponse1
        )

        val result = underTest.listGroupsForUserAsAdmin("USERNAME")
        result shouldContainExactlyInAnyOrder listOf("group1", "group2")

        // First time calls with no token
        val built1 = adminListGroupsConsumer[0]
        built1.username() shouldBe "USERNAME"
        built1.limit() shouldBe 50
        built1.nextToken() shouldBe null
        built1.userPoolId() shouldBe userPoolId

        adminListGroupsConsumer shouldHaveSize 1
    }

    @Test
    fun canListUsersInGroupWhenNoPagingNeeded() {
        val listUsersInGroupResponse = ListUsersInGroupResponse.builder()
            .users(
                UserType.builder().username("user1").attributes(
                    AttributeType.builder().name("key1").value("value1").build()
                ).build(),
                UserType.builder().username("user2").attributes(
                    AttributeType.builder().name("key2").value("value2").build()
                ).build()
            )
            .nextToken(null)
            .build()

        val listUsersInGroupRequests = mutableListOf<ListUsersInGroupRequest>()
        every { cognitoClient.listUsersInGroup(capture(listUsersInGroupRequests)) } returnsMany listOf(
            listUsersInGroupResponse
        )

        val result = underTest.listUsersInGroup("pro")
        result shouldContainExactlyInAnyOrder listOf(
            CognitoUser("user1", mapOf(Pair("key1", "value1"))),
            CognitoUser("user2", mapOf(Pair("key2", "value2"))),
        )

        // First time calls with no token
        val built1 = listUsersInGroupRequests[0]
        built1.limit() shouldBe 50
        built1.nextToken() shouldBe null
        built1.userPoolId() shouldBe userPoolId
        built1.groupName() shouldBe "pro"

        listUsersInGroupRequests shouldHaveSize 1
    }

    @Test
    fun canListUsersInGroupWhenPagingNeeded() {
        val listUsersInGroupResponse = ListUsersInGroupResponse.builder()
            .users(
                UserType.builder().username("user1").attributes(
                    AttributeType.builder().name("key1").value("value1").build()
                ).build(),
                UserType.builder().username("user2").attributes(
                    AttributeType.builder().name("key2").value("value2").build()
                ).build()
            )
            .nextToken("token1")
            .build()

        val listUsersInGroupResponse2 = ListUsersInGroupResponse.builder()
            .users(
                UserType.builder().username("user3").attributes(
                    AttributeType.builder().name("key3").value("value3").build()
                ).build()
            )
            .nextToken(null)
            .build()

        val listUsersInGroupRequests = mutableListOf<ListUsersInGroupRequest>()
        every { cognitoClient.listUsersInGroup(capture(listUsersInGroupRequests)) } returnsMany listOf(
            listUsersInGroupResponse, listUsersInGroupResponse2
        )

        val result = underTest.listUsersInGroup("pro")
        result shouldContainExactlyInAnyOrder listOf(
            CognitoUser("user1", mapOf(Pair("key1", "value1"))),
            CognitoUser("user2", mapOf(Pair("key2", "value2"))),
            CognitoUser("user3", mapOf(Pair("key3", "value3")))
        )

        // First time calls with no token
        val built1 = listUsersInGroupRequests[0]
        built1.limit() shouldBe 50
        built1.nextToken() shouldBe null
        built1.userPoolId() shouldBe userPoolId
        built1.groupName() shouldBe "pro"

        val built2 = listUsersInGroupRequests[1]
        built2.limit() shouldBe 50
        built2.nextToken() shouldBe "token1"
        built2.userPoolId() shouldBe userPoolId
        built2.groupName() shouldBe "pro"

        listUsersInGroupRequests shouldHaveSize 2
    }

    @Test
    fun canSearchUsers() {
        val listUsersResponse = ListUsersResponse.builder()
            .users(
                UserType.builder().username("user1").attributes(
                    AttributeType.builder().name("key1").value("value1").build()
                ).build(),
                UserType.builder().username("user2").attributes(
                    AttributeType.builder().name("key2").value("value2").build()
                ).build()
            )
            .build()

        val searchUserBuilder = slot<Consumer<ListUsersRequest.Builder>>()
        every { cognitoClient.listUsers(capture(searchUserBuilder)) } returns listUsersResponse

        val result = underTest.searchUsers(SearchableCognitoAttribute.EMAIL, "test@email.com", SearchType.EQUALS)

        val builder = ListUsersRequest.builder()
        searchUserBuilder.captured.accept(builder)

        val built = builder.build()
        built.userPoolId() shouldBe userPoolId
        built.filter() shouldBe "email = \"test@email.com\""

        result shouldContainExactlyInAnyOrder listOf(
            CognitoUser("user1", mapOf(Pair("key1", "value1"))),
            CognitoUser("user2", mapOf(Pair("key2", "value2")))
        )
    }

    @Test
    fun canSearchUsersWithDifferentParameters() {
        val listUsersResponse = ListUsersResponse.builder()
            .users(
                UserType.builder().username("user1").attributes(
                    AttributeType.builder().name("key1").value("value1").build()
                ).build(),
                UserType.builder().username("user2").attributes(
                    AttributeType.builder().name("key2").value("value2").build()
                ).build()
            )
            .build()

        val searchUserBuilder = slot<Consumer<ListUsersRequest.Builder>>()
        every { cognitoClient.listUsers(capture(searchUserBuilder)) } returns listUsersResponse

        val result = underTest.searchUsers(SearchableCognitoAttribute.USERNAME, "user", SearchType.PREFIX)

        val builder = ListUsersRequest.builder()
        searchUserBuilder.captured.accept(builder)

        val built = builder.build()
        built.userPoolId() shouldBe userPoolId
        built.filter() shouldBe "username ^= \"user\""

        result shouldContainExactlyInAnyOrder listOf(
            CognitoUser("user1", mapOf(Pair("key1", "value1"))),
            CognitoUser("user2", mapOf(Pair("key2", "value2")))
        )
    }

    @Test
    fun canAddUserToGroupAsAdmin() {
        val addUserToGroupRequest = slot<Consumer<AdminAddUserToGroupRequest.Builder>>()
        every { cognitoClient.adminAddUserToGroup(capture(addUserToGroupRequest)) } returns mockk()

        underTest.addUserToGroupAsAdmin("user", "group")

        val builder = AdminAddUserToGroupRequest.builder()
        addUserToGroupRequest.captured.accept(builder)

        val built = builder.build()
        built.userPoolId() shouldBe userPoolId
        built.username() shouldBe "user"
        built.groupName() shouldBe "group"
    }

    @Test
    fun canRemoveUserFromGroupAsAdmin() {
        val removeUserFromGroupRequest = slot<Consumer<AdminRemoveUserFromGroupRequest.Builder>>()
        every { cognitoClient.adminRemoveUserFromGroup(capture(removeUserFromGroupRequest)) } returns mockk()

        underTest.removeUserFromGroupAsAdmin("user", "group")

        val builder = AdminRemoveUserFromGroupRequest.builder()
        removeUserFromGroupRequest.captured.accept(builder)

        val built = builder.build()
        built.userPoolId() shouldBe userPoolId
        built.username() shouldBe "user"
        built.groupName() shouldBe "group"
    }

    @Test
    fun canSetUserAttributeAsAdmin() {
        val adminUpdateUserAttributesRequest = slot<Consumer<AdminUpdateUserAttributesRequest.Builder>>()
        every { cognitoClient.adminUpdateUserAttributes(capture(adminUpdateUserAttributesRequest)) } returns mockk()

        underTest.setAttributeAsAdmin("user", "key", "value")

        val builder = AdminUpdateUserAttributesRequest.builder()
        adminUpdateUserAttributesRequest.captured.accept(builder)

        val built = builder.build()
        built.userPoolId() shouldBe userPoolId
        built.username() shouldBe "user"
        built.userAttributes()[0].name() shouldBe "key"
        built.userAttributes()[0].value() shouldBe "value"
    }
}

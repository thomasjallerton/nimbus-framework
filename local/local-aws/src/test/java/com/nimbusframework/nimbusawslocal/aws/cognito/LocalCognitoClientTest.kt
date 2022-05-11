package com.nimbusframework.nimbusawslocal.aws.cognito

import com.nimbusframework.nimbusaws.clients.AwsClientBuilder
import com.nimbusframework.nimbusaws.clients.cognito.CognitoUser
import com.nimbusframework.nimbusaws.clients.cognito.SearchType
import com.nimbusframework.nimbusaws.clients.cognito.SearchableCognitoAttribute
import com.nimbusframework.nimbusawslocal.aws.AwsSpecificLocalDeployment
import com.nimbusframework.nimbuslocal.LocalNimbusDeployment
import exampleresources.UserPool
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

internal class LocalCognitoClientTest: StringSpec({

    "Can get user" {
        // given
        LocalNimbusDeployment.getNewInstance { it
            .withSpecificLocalDeployment(AwsSpecificLocalDeployment.newInstance())
            .withClasses(UserPool::class.java)
        }

        val userPool = AwsSpecificLocalDeployment.currentInstance().getUserPool(UserPool::class.java)

        userPool.addUser("ACCESS_TOKEN", CognitoUser("user", mapOf(Pair("key", "value"))))

        // when
        val underTest = AwsClientBuilder.getCognitoClient(UserPool::class.java)
        underTest.getUser("ACCESS_TOKEN") shouldBe CognitoUser("user", mapOf(Pair("key", "value")))
    }

    "Can list groups for user" {
        // given
        LocalNimbusDeployment.getNewInstance { it
            .withSpecificLocalDeployment(AwsSpecificLocalDeployment.newInstance())
            .withClasses(UserPool::class.java)
        }

        val userPool = AwsSpecificLocalDeployment.currentInstance().getUserPool(UserPool::class.java)

        userPool.addUser("ACCESS_TOKEN", CognitoUser("user", mapOf(Pair("key", "value"))))
        userPool.addUserToGroupAsAdmin("user", "group")
        userPool.addUserToGroupAsAdmin("user", "group_two")

        // when
        val underTest = AwsClientBuilder.getCognitoClient(UserPool::class.java)
        underTest.listGroupsForUserAsAdmin("user") shouldContainExactlyInAnyOrder listOf("group", "group_two")
    }

    "Can users in group" {
        // given
        LocalNimbusDeployment.getNewInstance { it
            .withSpecificLocalDeployment(AwsSpecificLocalDeployment.newInstance())
            .withClasses(UserPool::class.java)
        }

        val userPool = AwsSpecificLocalDeployment.currentInstance().getUserPool(UserPool::class.java)

        userPool.addUser("ACCESS_TOKEN", CognitoUser("user", mapOf(Pair("key", "value"))))
        userPool.addUserToGroupAsAdmin("user", "group")

        userPool.addUser("ACCESS_TOKEN_2", CognitoUser("user2", mapOf(Pair("key2", "value2"))))
        userPool.addUserToGroupAsAdmin("user2", "group")

        // when
        val underTest = AwsClientBuilder.getCognitoClient(UserPool::class.java)
        underTest.listUsersInGroup("group") shouldContainExactlyInAnyOrder listOf(
            CognitoUser("user", mapOf(Pair("key", "value"))),
            CognitoUser("user2", mapOf(Pair("key2", "value2")))
        )
    }

    "Can search for user with equals attribute" {
        // given
        LocalNimbusDeployment.getNewInstance { it
            .withSpecificLocalDeployment(AwsSpecificLocalDeployment.newInstance())
            .withClasses(UserPool::class.java)
        }

        val userPool = AwsSpecificLocalDeployment.currentInstance().getUserPool(UserPool::class.java)

        userPool.addUser("ACCESS_TOKEN", CognitoUser("user", mapOf(Pair("email", "value"))))
        userPool.addUser("ACCESS_TOKEN2", CognitoUser("user2", mapOf(Pair("email", "value"))))

        // when
        val underTest = AwsClientBuilder.getCognitoClient(UserPool::class.java)
        underTest.searchUsers(SearchableCognitoAttribute.EMAIL, "value") shouldContainExactlyInAnyOrder listOf(
            CognitoUser("user", mapOf(Pair("email", "value"))),
            CognitoUser("user2", mapOf(Pair("email", "value")))
        )
    }

    "Can search for user with prefix attribute" {
        // given
        LocalNimbusDeployment.getNewInstance { it
            .withSpecificLocalDeployment(AwsSpecificLocalDeployment.newInstance())
            .withClasses(UserPool::class.java)
        }

        val userPool = AwsSpecificLocalDeployment.currentInstance().getUserPool(UserPool::class.java)

        userPool.addUser("ACCESS_TOKEN", CognitoUser("user", mapOf(Pair("email", "value"))))
        userPool.addUser("ACCESS_TOKEN2", CognitoUser("user2", mapOf(Pair("email", "valmule"))))

        // when
        val underTest = AwsClientBuilder.getCognitoClient(UserPool::class.java)
        underTest.searchUsers(SearchableCognitoAttribute.EMAIL, "val", SearchType.PREFIX) shouldContainExactlyInAnyOrder listOf(
            CognitoUser("user", mapOf(Pair("email", "value"))),
            CognitoUser("user2", mapOf(Pair("email", "valmule")))
        )
    }

    "Can add user to group" {
        // given
        LocalNimbusDeployment.getNewInstance { it
            .withSpecificLocalDeployment(AwsSpecificLocalDeployment.newInstance())
            .withClasses(UserPool::class.java)
        }

        val userPool = AwsSpecificLocalDeployment.currentInstance().getUserPool(UserPool::class.java)
        userPool.addUser("ACCESS_TOKEN", CognitoUser("user", mapOf(Pair("key", "value"))))

        // when
        val underTest = AwsClientBuilder.getCognitoClient(UserPool::class.java)
        underTest.addUserToGroupAsAdmin("user", "group")
        underTest.addUserToGroupAsAdmin("user", "group_two")

        // then
        userPool.getGroupsForUser("user") shouldContainExactlyInAnyOrder listOf("group", "group_two")
    }

    "Can remove user from group" {
        // given
        LocalNimbusDeployment.getNewInstance { it
            .withSpecificLocalDeployment(AwsSpecificLocalDeployment.newInstance())
            .withClasses(UserPool::class.java)
        }

        val userPool = AwsSpecificLocalDeployment.currentInstance().getUserPool(UserPool::class.java)

        userPool.addUser("ACCESS_TOKEN", CognitoUser("user", mapOf(Pair("key", "value"))))
        userPool.addUserToGroupAsAdmin("user", "group")
        userPool.addUserToGroupAsAdmin("user", "group_two")

        // when
        val underTest = AwsClientBuilder.getCognitoClient(UserPool::class.java)
        underTest.removeUserFromGroupAsAdmin("user", "group")

        // then
        userPool.getGroupsForUser("user") shouldContainExactlyInAnyOrder listOf("group_two")
    }

    "Can set attribute for user" {
        // given
        LocalNimbusDeployment.getNewInstance { it
            .withSpecificLocalDeployment(AwsSpecificLocalDeployment.newInstance())
            .withClasses(UserPool::class.java)
        }

        val userPool = AwsSpecificLocalDeployment.currentInstance().getUserPool(UserPool::class.java)

        userPool.addUser("ACCESS_TOKEN", CognitoUser("user", mapOf(Pair("key", "value"))))

        // when
        val underTest = AwsClientBuilder.getCognitoClient(UserPool::class.java)
        underTest.setAttributeAsAdmin("user", "new_key", "new_val")

        // then
        userPool.getUserByUsername("user") shouldBe CognitoUser("user", mapOf(Pair("key", "value"), Pair("new_key", "new_val")))
        userPool.getUser("ACCESS_TOKEN") shouldBe CognitoUser("user", mapOf(Pair("key", "value"), Pair("new_key", "new_val")))
    }

})

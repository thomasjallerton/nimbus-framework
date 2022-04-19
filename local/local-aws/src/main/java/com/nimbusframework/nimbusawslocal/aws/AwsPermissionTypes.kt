package com.nimbusframework.nimbusawslocal.aws

import com.nimbusframework.nimbuscore.permissions.PermissionType

object AwsPermissionTypes {

    val COGNITO: PermissionType = object: PermissionType {
        override fun getKey(): String {
            return "COGNITO"
        }
    }

    val COGNITO_ADMIN: PermissionType = object: PermissionType {
        override fun getKey(): String {
            return "COGNITO_ADMIN"
        }
    }

}

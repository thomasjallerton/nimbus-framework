package com.nimbusframework.nimbuscore.permissions

interface PermissionType {

    fun getKey(): String

    companion object {

        val FILE_STORAGE: PermissionType = object:PermissionType {
            override fun getKey(): String {
                return "FILE_STORAGE"
            }
        }

        val NOTIFICATION_TOPIC = object : PermissionType {
            override fun getKey(): String {
                return "NOTIFICATION_TOPIC"
            }
        }
        val BASIC_FUNCTION = object : PermissionType {
            override fun getKey(): String {
                return "BASIC_FUNCTION"
            }
        }
        val DOCUMENT_STORE = object : PermissionType {
            override fun getKey(): String {
                return "DOCUMENT_STORE"
            }
        }
        val FUNCTION = object : PermissionType {
            override fun getKey(): String {
                return "FUNCTION"
            }
        }
        val KEY_VALUE_STORE = object : PermissionType {
            override fun getKey(): String {
                return "KEY_VALUE_STORE"
            }
        }
        val QUEUE = object : PermissionType {
            override fun getKey(): String {
                return "QUEUE"
            }
        }
        val RELATIONAL_DATABASE = object : PermissionType {
            override fun getKey(): String {
                return "NOTIFICATION_TOPIC"
            }
        }
        val WEBSOCKET_MANAGER = object : PermissionType {
            override fun getKey(): String {
                return "RELATIONAL_DATABASE"
            }
        }

    }
}

package com.nimbusframework.nimbusaws.cloudformation.generation.resources.database

import com.nimbusframework.nimbusaws.annotation.annotations.database.ParsedDatabaseConfig
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.NimbusEnvironmentVariable

class DatabaseUsernameEnvironmentVariable(
    databaseConfig: ParsedDatabaseConfig
): NimbusEnvironmentVariable<ParsedDatabaseConfig>(databaseConfig) {

    override fun getKey(): String {
        return annotation.name + "_USERNAME"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DatabaseUsernameEnvironmentVariable) return false
        return getKey() == other.getKey()
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

}

package com.nimbusframework.nimbusaws.cloudformation.generation.resources.database

import com.nimbusframework.nimbusaws.annotation.annotations.database.ParsedDatabaseConfig
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.NimbusEnvironmentVariable

class DatabasePasswordUrlEnvironmentVariable(
    databaseConfig: ParsedDatabaseConfig
): NimbusEnvironmentVariable<ParsedDatabaseConfig>(databaseConfig) {

    override fun getKey(): String {
        return annotation.name + "_PASSWORD"
    }

}

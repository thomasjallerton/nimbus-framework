package com.nimbusframework.nimbuslocal.exampleModels

import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition
import com.nimbusframework.nimbuscore.annotations.document.DocumentStoreDefinition
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute
import com.nimbusframework.nimbuscore.annotations.persistent.Key

@RelationalDatabaseDefinition(
        name = "maindb",
        username = "username",
        password = "password",
        databaseLanguage = DatabaseLanguage.POSTGRESQL
)
class Database
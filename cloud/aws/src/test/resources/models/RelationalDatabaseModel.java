package models;

import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage;
import com.nimbusframework.nimbuscore.annotations.database.DatabaseSize;
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition;

@RelationalDatabaseDefinition(
    name = "testRelationalDatabase",
    username = "username",
    password = "password",
    databaseClass = DatabaseSize.FREE,
    databaseLanguage = DatabaseLanguage.MYSQL,
    allocatedSizeGB = 30
)
public class RelationalDatabaseModel {}

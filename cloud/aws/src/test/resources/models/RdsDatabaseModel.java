package models;

import com.nimbusframework.nimbusaws.annotation.annotations.database.RdsDatabase;
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage;

@RdsDatabase(
    name = "testRdsDatabase",
    username = "username",
    password = "password",
    awsDatabaseInstance = "micro",
    databaseLanguage = DatabaseLanguage.MYSQL,
    allocatedSizeGB = 30
)
public class RdsDatabaseModel {}

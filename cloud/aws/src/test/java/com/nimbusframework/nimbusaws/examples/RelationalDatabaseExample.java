package com.nimbusframework.nimbusaws.examples;

import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage;
import com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition;

@RelationalDatabaseDefinition(name = "testDb", username = "${USERNAME}", password = "${PASSWORD}", databaseLanguage = DatabaseLanguage.POSTGRESQL)
public class RelationalDatabaseExample {}

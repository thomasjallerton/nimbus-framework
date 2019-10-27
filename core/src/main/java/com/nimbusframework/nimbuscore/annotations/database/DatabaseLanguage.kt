package com.nimbusframework.nimbuscore.annotations.database

enum class DatabaseLanguage {
    MYSQL, POSTGRESQL, ORACLE, MARIADB, SQLSERVER;


    fun toEngine(size: DatabaseSize): String {
        return if (size == DatabaseSize.FREE) {
            when (this) {
                MYSQL -> "mysql"
                ORACLE -> "oracle-ee"
                MARIADB -> "mariadb"
                SQLSERVER -> "sqlserver-ex"
                POSTGRESQL -> "postgres"
            }
        } else {
            when (this) {
                MYSQL -> "aurora-mysql"
                ORACLE -> "oracle-ee"
                MARIADB -> "mariadb"
                SQLSERVER -> "sqlserver-ee"
                POSTGRESQL -> "aurora-postgresql"
            }
        }
    }
}

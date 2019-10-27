package com.nimbusframework.nimbuscore.annotations.database

enum class DatabaseSize {
    FREE, SMALL, MEDIUM, LARGE, XLARGE, MAXIMUM;

    fun toInstanceClass(): String {
        return when(this) {
            FREE -> "db.t2.micro"
            SMALL -> "db.t2.small"
            MEDIUM -> "db.t3.medium"
            LARGE -> "db.r4.large"
            XLARGE -> "db.r5.2xlarge"
            MAXIMUM -> "db.r5.12xlarge"
        }
    }
}

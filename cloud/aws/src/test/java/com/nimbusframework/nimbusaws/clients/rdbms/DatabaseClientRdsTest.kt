package com.nimbusframework.nimbusaws.clients.rdbms

import com.nimbusframework.nimbusaws.annotation.annotations.database.ParsedDatabaseConfig
import com.nimbusframework.nimbusaws.clients.InternalEnvironmentVariableClient
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.database.DatabaseConnectionUrlEnvironmentVariable
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.database.DatabasePasswordEnvironmentVariable
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.database.DatabaseUsernameEnvironmentVariable
import com.nimbusframework.nimbuscore.annotations.database.DatabaseLanguage
import io.kotest.core.spec.style.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import org.postgresql.util.PSQLException
import kotlin.test.assertFailsWith

class DatabaseClientRdsTest : AnnotationSpec() {

    private lateinit var underTest: DatabaseClientRds

    private lateinit var environmentVariableClient: InternalEnvironmentVariableClient

    private val parsedDatabaseConfig = ParsedDatabaseConfig(
        "name",
        "USERNAME",
        "PASSWORD",
        DatabaseLanguage.POSTGRESQL,
        "size",
        1
    )

    @BeforeEach
    fun setup() {
        environmentVariableClient = mockk()

        every { environmentVariableClient.get(eq(DatabaseConnectionUrlEnvironmentVariable(parsedDatabaseConfig))) } returns "URL"
        every { environmentVariableClient.get(eq(DatabaseUsernameEnvironmentVariable(parsedDatabaseConfig))) } returns "USERNAME"
        every { environmentVariableClient.get(eq(DatabasePasswordEnvironmentVariable(parsedDatabaseConfig))) } returns "PASSWORD"

        underTest = DatabaseClientRds(parsedDatabaseConfig, environmentVariableClient)
    }

    @Test
    fun canConnectToDatabase() {
        assertFailsWith<PSQLException> { underTest.getConnection() }
    }

    @Test
    fun canConnectToDatabaseSlashAtEndOfUrl() {
        every { environmentVariableClient.get(eq(DatabaseConnectionUrlEnvironmentVariable(parsedDatabaseConfig))) } returns "URL"
        assertFailsWith<PSQLException> { underTest.getConnection() }
    }

    @Test
    fun canConnectToDatabaseWithExtraParams() {
        assertFailsWith<PSQLException> { underTest.getConnection("database", true) }
    }


}

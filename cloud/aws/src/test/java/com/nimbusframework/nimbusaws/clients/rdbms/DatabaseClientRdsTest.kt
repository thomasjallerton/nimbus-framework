package com.nimbusframework.nimbusaws.clients.rdbms

import com.nimbusframework.nimbusaws.examples.RelationalDatabaseExample
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import io.kotest.core.spec.style.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import org.postgresql.util.PSQLException
import kotlin.test.assertFailsWith

class DatabaseClientRdsTest : AnnotationSpec() {

    private lateinit var underTest: DatabaseClientRds<RelationalDatabaseExample>

    private lateinit var environmentVariableClient: EnvironmentVariableClient

    @BeforeEach
    fun setup() {
        environmentVariableClient = mockk()
        underTest = DatabaseClientRds(RelationalDatabaseExample::class.java, environmentVariableClient)
    }

    @Test
    fun canConnectToDatabase() {
        every { environmentVariableClient.get("testDbRdsInstance_CONNECTION_URL") } returns "URL"
        every { environmentVariableClient.get("testDbRdsInstance_USERNAME") } returns "USERNAME"
        every { environmentVariableClient.get("testDbRdsInstance_PASSWORD") } returns "PASSWORD"

        assertFailsWith<PSQLException> { underTest.getConnection() }
    }

    @Test
    fun canConnectToDatabaseSlashAtEndOfUrl() {
        every { environmentVariableClient.get("testDbRdsInstance_CONNECTION_URL") } returns "URL/"
        every { environmentVariableClient.get("testDbRdsInstance_USERNAME") } returns "USERNAME"
        every { environmentVariableClient.get("testDbRdsInstance_PASSWORD") } returns "PASSWORD"

        assertFailsWith<PSQLException> { underTest.getConnection() }
    }

    @Test
    fun canConnectToDatabaseWithExtraParams() {
        every { environmentVariableClient.get("testDbRdsInstance_CONNECTION_URL") } returns "URL"
        every { environmentVariableClient.get("testDbRdsInstance_USERNAME") } returns "USERNAME"
        every { environmentVariableClient.get("testDbRdsInstance_PASSWORD") } returns "PASSWORD"

        assertFailsWith<PSQLException> { underTest.getConnection("database", true) }
    }


}

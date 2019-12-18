package com.nimbusframework.nimbusaws.clients.rdbms

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nimbusframework.nimbusaws.examples.RelationalDatabaseExample
import com.nimbusframework.nimbuscore.clients.function.EnvironmentVariableClient
import io.kotlintest.specs.AnnotationSpec
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
        underTest = DatabaseClientRds(RelationalDatabaseExample::class.java)
        val injector = Guice.createInjector(object: AbstractModule() {
            override fun configure() {
                bind(EnvironmentVariableClient::class.java).toInstance(environmentVariableClient)
            }
        })
        injector.injectMembers(underTest)
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
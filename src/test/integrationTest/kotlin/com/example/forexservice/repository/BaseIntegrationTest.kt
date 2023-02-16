package com.example.forexservice.repository

import org.slf4j.LoggerFactory
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration

internal class IntegrationTestInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val dbContainer = PostgreSQLContainer("postgres")
            .withDatabaseName("forexdb")
            .withUsername("postgres")
            .withPassword("YourStrong@Passw0rd")
            .withExposedPorts(5432)
            .withLogConsumer(Slf4jLogConsumer(log))

        dbContainer.waitingFor(
            Wait.forListeningPort()
                .withStartupTimeout(Duration.ofSeconds(20))
        )
        dbContainer.start()
        TestPropertyValues.of(
            "spring.datasource.url=" + dbContainer.jdbcUrl,
            "spring.datasource.username=" + dbContainer.username,
            "spring.datasource.password=" + dbContainer.password
        ).applyTo(applicationContext.environment)
    }
}

@DataJpaTest
@ContextConfiguration(initializers = [IntegrationTestInitializer::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlGroup(
    Sql(scripts = ["classpath:db/initTestData.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["classpath:db/clearTestData.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
abstract class BaseIntegrationTest
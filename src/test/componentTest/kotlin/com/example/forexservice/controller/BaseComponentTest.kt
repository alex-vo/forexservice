package com.example.forexservice.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType.TEXT_XML
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.time.Duration


fun MockServerClient.setupECBResponse(body: String) {
    reset()
        .`when`(
            request()
                .withMethod("GET")
                .withPath("/stats/eurofxref/eurofxref-daily.xml")
        ).respond(
            response()
                .withBody(body)
                .withStatusCode(200)
                .withContentType(TEXT_XML)
        )
}

internal class ComponentTestInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        lateinit var mockServerContainer: MockServerContainer
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        mockServerContainer = MockServerContainer(DockerImageName.parse("mockserver/mockserver"))
        mockServerContainer.start()

        val mockServerClient = MockServerClient(mockServerContainer.host, mockServerContainer.serverPort)
        mockServerClient.setupECBResponse("src/test/componentTest/resources/integration/ecb-api-first-response.xml".asResourceContent())

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
            "spring.datasource.url=${dbContainer.jdbcUrl}",
            "spring.datasource.username=${dbContainer.username}",
            "spring.datasource.password=${dbContainer.password}",
            "integration.ecb.base-uri=http://${mockServerContainer.host}:${mockServerContainer.serverPort}",
        ).applyTo(applicationContext.environment)
    }

}

@SpringBootTest
@ContextConfiguration(initializers = [ComponentTestInitializer::class])
@AutoConfigureMockMvc
@SqlGroup(
    Sql(scripts = ["classpath:db/initTestData.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    Sql(scripts = ["classpath:db/clearTestData.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
abstract class BaseComponentTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    private lateinit var mockServerClient: MockServerClient

    protected fun getMockServerClient(): MockServerClient {
        if (!this::mockServerClient.isInitialized) {
            mockServerClient = MockServerClient(
                ComponentTestInitializer.mockServerContainer.host,
                ComponentTestInitializer.mockServerContainer.serverPort
            )
        }
        return mockServerClient
    }

    @BeforeEach
    fun beforeEach() {
        getMockServerClient()
            .setupECBResponse("src/test/componentTest/resources/integration/ecb-api-first-response.xml".asResourceContent())
    }

    val jacksonObjectMapper: ObjectMapper = jacksonObjectMapper()
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)

    final inline fun <reified R> ResultActionsDsl.getResponseDTO(): R =
        andReturn()
            .response
            .contentAsString
            .asDTO()

    final inline fun <reified R> String.asDTO(): R =
        jacksonObjectMapper.readValue(this, object : TypeReference<R>() {})

    fun performAdminPut(url: String, body: String? = null): ResultActionsDsl =
        performPut(url, body) {
            adminCredentials()
        }

    fun performAdminDelete(url: String): ResultActionsDsl =
        performDelete(url) {
            adminCredentials()
        }

    fun performAdminGet(url: String): ResultActionsDsl =
        performGet(url) {
            adminCredentials()
        }

    fun performAdminPost(url: String, body: String? = null): ResultActionsDsl =
        performPost(url, body) {
            adminCredentials()
        }

    fun performSuccessfulAdminPut(url: String) {
        performSuccessfulAdminPut(url, null)
    }

    fun <T> performSuccessfulAdminPut(url: String, body: T?) {
        performAdminPut(url, jacksonObjectMapper.writeValueAsString(body))
            .andExpect { status { isOk() } }
    }

    fun performSuccessfulAdminDelete(url: String) {
        performAdminDelete(url)
            .andExpect { status { isOk() } }
    }

    final inline fun <reified T> performSuccessfulAdminGet(url: String): T =
        performAdminGet(url)
            .andExpect { status { isOk() } }
            .getResponseDTO()

    fun <T> performSuccessfulAdminPost(url: String, body: T) {
        performAdminPost(url, jacksonObjectMapper.writeValueAsString(body))
            .andExpect { status { isOk() } }
    }

    final inline fun <reified T> performSuccessfulUserGet(url: String): T =
        performUserGet(url)
            .andExpect { status { isOk() } }
            .getResponseDTO()

    fun performUserGet(url: String): ResultActionsDsl =
        performGet(url) {
            userCredentials()
        }

    fun performPut(
        url: String,
        body: String? = null,
        dsl: MockHttpServletRequestDsl.() -> Unit = {}
    ): ResultActionsDsl =
        mockMvc.put(url) {
            dsl()
            contentType = MediaType.APPLICATION_JSON
            content = body
        }

    fun performDelete(url: String, dsl: MockHttpServletRequestDsl.() -> Unit = {}): ResultActionsDsl =
        mockMvc.delete(url) {
            dsl()
        }

    fun performGet(url: String, dsl: MockHttpServletRequestDsl.() -> Unit = {}): ResultActionsDsl =
        mockMvc.get(url, dsl = dsl)

    fun performPost(
        url: String,
        body: String? = null,
        dsl: MockHttpServletRequestDsl.() -> Unit = {}
    ): ResultActionsDsl =
        mockMvc.post(url) {
            dsl()
            contentType = MediaType.APPLICATION_JSON
            content = body
        }

    fun MockHttpServletRequestDsl.adminCredentials() = with(httpBasic("admin", "123"))

    fun MockHttpServletRequestDsl.userCredentials() = with(httpBasic("user", "123"))

}

fun String.asResourceContent(): String = File(this).readText()


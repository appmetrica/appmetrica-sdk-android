package io.appmetrica.analytics.networkokhttp.impl

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request
import io.appmetrica.analytics.testutils.CommonTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException

internal class CallImplTest : CommonTest() {

    private lateinit var mockWebServer: MockWebServer
    private val okHttpClient = OkHttpClient()
    private val settings = NetworkClientSettings.Builder()
        .withMaxResponseSize(1024 * 1024)
        .build()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `execute with successful GET request`() {
        val responseBody = "success response"
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
        )

        val url = mockWebServer.url("/test").toString()
        val request = Request.Builder(url).build()
        val call = CallImpl(okHttpClient, request, settings)

        val response = call.execute()

        ObjectPropertyAssertions(response)
            .checkField("isCompleted", true)
            .checkField("code", 200)
            .checkField("responseData", responseBody.toByteArray())
            .checkField("headers", mapOf("Content-Length" to listOf("${responseBody.length}")))
            .checkFieldIsNull("exception")
            .checkField("url", mockWebServer.url("/test").toString())
            .checkAll()
    }

    @Test
    fun `execute with error response`() {
        val errorBody = "error response"
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody(errorBody)
        )

        val url = mockWebServer.url("/test").toString()
        val request = Request.Builder(url).build()
        val call = CallImpl(okHttpClient, request, settings)

        val response = call.execute()

        ObjectPropertyAssertions(response)
            .checkField("isCompleted", true)
            .checkField("code", 404)
            .checkField("responseData", errorBody.toByteArray())
            .checkField("headers", mapOf("Content-Length" to listOf("${errorBody.length}")))
            .checkFieldIsNull("exception")
            .checkField("url", mockWebServer.url("/test").toString())
            .checkAll()
    }

    @Test
    fun `execute with POST request`() {
        val requestBody = "test body"
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody("created")
        )

        val url = mockWebServer.url("/test").toString()
        val request = Request.Builder(url)
            .withMethod(Request.Method.POST)
            .withBody(requestBody.toByteArray())
            .build()
        val call = CallImpl(okHttpClient, request, settings)

        val response = call.execute()

        SoftAssertions().apply {
            assertThat(response.isCompleted).`as`("isCompleted").isTrue()
            assertThat(response.code).`as`("code").isEqualTo(201)
            assertAll()
        }

        val recordedRequest = mockWebServer.takeRequest()
        SoftAssertions().apply {
            assertThat(recordedRequest.method).`as`("method").isEqualTo("POST")
            assertThat(recordedRequest.body.readUtf8()).`as`("body").isEqualTo(requestBody)
            assertAll()
        }
    }

    @Test
    fun `execute with custom headers`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("ok")
        )

        val url = mockWebServer.url("/test").toString()
        val request = Request.Builder(url)
            .addHeader("User-Agent", "TestClient/1.0")
            .addHeader("Authorization", "Bearer token123")
            .build()
        val call = CallImpl(okHttpClient, request, settings)

        val response = call.execute()

        assertThat(response.isCompleted).isTrue()

        val recordedRequest = mockWebServer.takeRequest()
        SoftAssertions().apply {
            assertThat(recordedRequest.getHeader("User-Agent")).`as`("User-Agent")
                .isEqualTo("TestClient/1.0")
            assertThat(recordedRequest.getHeader("Authorization")).`as`("Authorization")
                .isEqualTo("Bearer token123")
            assertAll()
        }
    }

    @Test
    fun `execute with response headers including multiple values`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("ok")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Custom-Header", "value1")
                .addHeader("X-Custom-Header", "value2")
        )

        val url = mockWebServer.url("/test").toString()
        val request = Request.Builder(url).build()
        val call = CallImpl(okHttpClient, request, settings)

        val response = call.execute()

        SoftAssertions().apply {
            assertThat(response.isCompleted).`as`("isCompleted").isTrue()
            assertThat(response.headers).`as`("contains Content-Type").containsKey("Content-Type")
            assertThat(response.headers["Content-Type"]).`as`("Content-Type value").contains("application/json")
            assertThat(response.headers["X-Custom-Header"]).`as`("X-Custom-Header values")
                .containsExactly("value1", "value2")
            assertAll()
        }
    }

    @Test
    fun `execute with network error returns error response`() {
        val request = Request.Builder("https://invalid.nonexistent.domain.test").build()
        val call = CallImpl(okHttpClient, request, settings)

        val response = call.execute()

        SoftAssertions().apply {
            assertThat(response.isCompleted).`as`("isCompleted").isFalse()
            assertThat(response.exception).`as`("exception").isInstanceOf(UnknownHostException::class.java)
            assertAll()
        }
    }

    @Test
    fun `execute with 500 error returns error data`() {
        val errorBody = "Internal Server Error"
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody(errorBody)
        )

        val url = mockWebServer.url("/test").toString()
        val request = Request.Builder(url).build()
        val call = CallImpl(okHttpClient, request, settings)

        val response = call.execute()

        SoftAssertions().apply {
            assertThat(response.isCompleted).`as`("isCompleted").isTrue()
            assertThat(response.code).`as`("code").isEqualTo(500)
            assertThat(String(response.responseData)).`as`("responseData").isEqualTo(errorBody)
            assertAll()
        }
    }

    @Test
    fun `execute with redirect response follows redirect`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(302)
                .setHeader("Location", "/redirected")
        )
        val content = "redirected content"
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(content)
        )

        val url = mockWebServer.url("/test").toString()
        val request = Request.Builder(url).build()
        val call = CallImpl(okHttpClient, request, settings)

        val response = call.execute()

        ObjectPropertyAssertions(response)
            .checkField("isCompleted", true)
            .checkField("code", 200)
            .checkField("responseData", content.toByteArray())
            .checkField("headers", mapOf("Content-Length" to listOf("${content.length}")))
            .checkFieldIsNull("exception")
            .checkField("url", mockWebServer.url("/redirected").toString())
            .checkAll()
    }
}

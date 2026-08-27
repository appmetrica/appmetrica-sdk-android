package io.appmetrica.analytics.networklegacy.impl

import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal class CallImplTest : CommonTest() {

    private val timeProvider = mock<TimeProvider>()
    private val connection = mock<HttpsURLConnection>()

    private val settings = NetworkClientSettings.Builder()
        .withConnectTimeout(5000)
        .withReadTimeout(10000)
        .withMaxResponseSize(1024 * 1024)
        .build()
    private val request = Request.Builder("https://example.com").build()

    private fun callImpl(
        settingsOverride: NetworkClientSettings = settings,
        requestOverride: Request = request,
        factory: (String) -> HttpsURLConnection? = { connection },
    ) = CallImpl(settingsOverride, requestOverride, timeProvider, factory)

    @Test
    fun `execute returns error when connection factory returns null`() {
        val call = callImpl(factory = { null })

        val response = call.execute()

        SoftAssertions().apply {
            assertThat(response.isCompleted).describedAs("isCompleted").isFalse()
            assertThat(response.exception).describedAs("exception").isInstanceOf(IllegalArgumentException::class.java)
            assertThat(response.exception?.message).describedAs("exception message")
                .contains("does not represent https connection")
            assertAll()
        }
    }

    @Test
    fun `execute returns error when connection factory throws`() {
        val cause = IOException("connection refused")
        val call = callImpl(factory = { throw cause })

        val response = call.execute()

        SoftAssertions().apply {
            assertThat(response.isCompleted).describedAs("isCompleted").isFalse()
            assertThat(response.exception).describedAs("exception").isSameAs(cause)
            assertAll()
        }
    }

    @Test
    fun `execute successful response with collectMetrics false - metrics is null`() {
        whenever(timeProvider.currentTimeMillis()).thenReturn(100L, 200L, 200L, 250L)
        whenever(connection.responseCode).thenReturn(200)
        whenever(connection.inputStream).thenReturn(ByteArrayInputStream("ok".toByteArray()))
        whenever(connection.headerFields).thenReturn(emptyMap())
        whenever(connection.url).thenReturn(URL("https://example.com"))

        val response = callImpl().execute()

        SoftAssertions().apply {
            assertThat(response.isCompleted).describedAs("isCompleted").isTrue()
            assertThat(response.code).describedAs("code").isEqualTo(200)
            assertThat(response.metrics).describedAs("metrics").isNull()
            assertAll()
        }
    }

    @Test
    fun `execute successful response with collectMetrics true - metrics contain timing`() {
        val settingsWithMetrics = NetworkClientSettings.Builder()
            .withMaxResponseSize(1024 * 1024)
            .withCollectMetrics(true)
            .build()
        whenever(timeProvider.currentTimeMillis()).thenReturn(100L, 200L, 200L, 250L)
        whenever(connection.responseCode).thenReturn(200)
        whenever(connection.inputStream).thenReturn(ByteArrayInputStream("ok".toByteArray()))
        whenever(connection.headerFields).thenReturn(emptyMap())
        whenever(connection.url).thenReturn(URL("https://example.com"))

        val response = callImpl(settingsOverride = settingsWithMetrics).execute()

        SoftAssertions().apply {
            assertThat(response.isCompleted).describedAs("isCompleted").isTrue()
            assertThat(response.metrics).describedAs("metrics").isNotNull()
            assertThat(response.metrics!!.timeToFirstByte).describedAs("timeToFirstByte").isEqualTo(100L) // 200-100
            assertThat(response.metrics!!.response).describedAs("response").isEqualTo(50L) // 250-200
            assertThat(response.metrics!!.dnsLookup).describedAs("dnsLookup").isNull()
            assertThat(response.metrics!!.tcpConnect).describedAs("tcpConnect").isNull()
            assertAll()
        }
    }

    @Test
    fun `execute error response uses errorStream`() {
        whenever(timeProvider.currentTimeMillis()).thenReturn(100L, 200L, 200L, 210L)
        whenever(connection.responseCode).thenReturn(404)
        whenever(connection.errorStream).thenReturn(ByteArrayInputStream("not found".toByteArray()))
        whenever(connection.headerFields).thenReturn(emptyMap())
        whenever(connection.url).thenReturn(URL("https://example.com"))

        val response = callImpl().execute()

        SoftAssertions().apply {
            assertThat(response.isCompleted).describedAs("isCompleted").isTrue()
            assertThat(response.code).describedAs("code").isEqualTo(404)
            assertThat(response.responseData).describedAs("responseData")
                .isEqualTo("not found".toByteArray())
            assertAll()
        }
    }

    @Test
    fun `execute returns error on exception during connection`() {
        val cause = IOException("timeout")
        whenever(connection.responseCode).thenThrow(cause)
        whenever(timeProvider.currentTimeMillis()).thenReturn(100L)

        val response = callImpl().execute()

        SoftAssertions().apply {
            assertThat(response.isCompleted).describedAs("isCompleted").isFalse()
            assertThat(response.exception).describedAs("exception").isSameAs(cause)
            assertAll()
        }
    }

    @Test
    fun `execute applies request headers to connection`() {
        val requestWithHeaders = Request.Builder("https://example.com")
            .addHeader("Authorization", "Bearer token")
            .addHeader("Accept", "application/json")
            .build()
        whenever(timeProvider.currentTimeMillis()).thenReturn(100L, 200L, 200L, 210L)
        whenever(connection.responseCode).thenReturn(200)
        whenever(connection.inputStream).thenReturn(ByteArrayInputStream(ByteArray(0)))
        whenever(connection.headerFields).thenReturn(emptyMap())

        callImpl(requestOverride = requestWithHeaders).execute()

        verify(connection).addRequestProperty("Authorization", "Bearer token")
        verify(connection).addRequestProperty("Accept", "application/json")
    }
}

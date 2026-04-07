package io.appmetrica.analytics.networkokhttp.impl

import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.testutils.CommonTest
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Protocol
import okhttp3.Request
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy

internal class NetworkCallMetricsListenerTest : CommonTest() {

    private val timeProvider = mock<TimeProvider>()
    private val call = mock<Call>()
    private val connection = mock<Connection> {
        on { protocol() } doReturn Protocol.HTTP_2
    }
    private val request = Request.Builder().url("https://example.com").build()

    private val listener = NetworkCallMetricsListener(timeProvider)

    @Test
    fun `buildMetrics with full new connection`() {
        whenever(timeProvider.currentTimeMillis())
            .thenReturn(100L) // dnsStart
            .thenReturn(110L) // dnsEnd
            .thenReturn(120L) // connectStart
            .thenReturn(140L) // connectEnd
            .thenReturn(150L) // secureConnectStart
            .thenReturn(165L) // secureConnectEnd
            .thenReturn(170L) // requestHeadersEnd
            .thenReturn(200L) // responseHeadersStart
            .thenReturn(250L) // responseBodyEnd

        listener.dnsStart(call, "example.com")
        listener.dnsEnd(call, "example.com", emptyList())
        listener.connectStart(call, InetSocketAddress(80), Proxy.NO_PROXY)
        listener.connectEnd(call, InetSocketAddress(80), Proxy.NO_PROXY, null)
        listener.secureConnectStart(call)
        listener.secureConnectEnd(call, null)
        listener.requestHeadersEnd(call, request)
        listener.responseHeadersStart(call)
        listener.responseBodyEnd(call, 0L)
        listener.connectionAcquired(call, connection)

        val metrics = listener.buildMetrics()

        assertThat(metrics.dnsLookup).isEqualTo(10L)
        assertThat(metrics.tcpConnect).isEqualTo(20L)
        assertThat(metrics.tlsHandshake).isEqualTo(15L)
        assertThat(metrics.timeToFirstByte).isEqualTo(30L)
        assertThat(metrics.response).isEqualTo(50L)
        assertThat(metrics.connectionReused).isFalse()
        assertThat(metrics.protocol).isEqualTo("h2")
    }

    @Test
    fun `buildMetrics with reused connection`() {
        whenever(timeProvider.currentTimeMillis())
            .thenReturn(170L) // requestHeadersEnd
            .thenReturn(200L) // responseHeadersStart
            .thenReturn(250L) // responseBodyEnd

        listener.connectionAcquired(call, connection)
        listener.requestHeadersEnd(call, request)
        listener.responseHeadersStart(call)
        listener.responseBodyEnd(call, 0L)

        val metrics = listener.buildMetrics()

        assertThat(metrics.dnsLookup).isNull()
        assertThat(metrics.tcpConnect).isNull()
        assertThat(metrics.tlsHandshake).isNull()
        assertThat(metrics.timeToFirstByte).isEqualTo(30L)
        assertThat(metrics.response).isEqualTo(50L)
        assertThat(metrics.connectionReused).isTrue()
        assertThat(metrics.protocol).isEqualTo("h2")
    }

    @Test
    fun `buildMetrics with no events returns all nulls and defaults`() {
        val metrics = listener.buildMetrics()

        assertThat(metrics.dnsLookup).isNull()
        assertThat(metrics.tcpConnect).isNull()
        assertThat(metrics.tlsHandshake).isNull()
        assertThat(metrics.timeToFirstByte).isNull()
        assertThat(metrics.response).isNull()
        assertThat(metrics.connectionReused).isFalse()
        assertThat(metrics.protocol).isNull()
    }

    @Test
    fun `callFailed fills incomplete phases`() {
        whenever(timeProvider.currentTimeMillis())
            .thenReturn(100L) // dnsStart
            .thenReturn(110L) // connectStart
            .thenReturn(120L) // secureConnectStart
            .thenReturn(130L) // responseHeadersStart
            .thenReturn(200L) // callFailed

        listener.dnsStart(call, "example.com")
        listener.connectStart(call, InetSocketAddress(80), Proxy.NO_PROXY)
        listener.secureConnectStart(call)
        listener.responseHeadersStart(call)
        listener.callFailed(call, IOException())

        val metrics = listener.buildMetrics()

        assertThat(metrics.dnsLookup).isEqualTo(100L)
        assertThat(metrics.tcpConnect).isEqualTo(90L)
        assertThat(metrics.tlsHandshake).isEqualTo(80L)
        assertThat(metrics.response).isEqualTo(70L)
    }

    @Test
    fun `callFailed does not overwrite completed phases`() {
        whenever(timeProvider.currentTimeMillis())
            .thenReturn(100L) // dnsStart
            .thenReturn(110L) // dnsEnd
            .thenReturn(120L) // connectStart
            .thenReturn(130L) // connectEnd
            .thenReturn(140L) // secureConnectStart
            .thenReturn(150L) // secureConnectEnd
            .thenReturn(160L) // responseHeadersStart
            .thenReturn(170L) // responseBodyEnd
            .thenReturn(999L) // callFailed

        listener.dnsStart(call, "example.com")
        listener.dnsEnd(call, "example.com", emptyList())
        listener.connectStart(call, InetSocketAddress(80), Proxy.NO_PROXY)
        listener.connectEnd(call, InetSocketAddress(80), Proxy.NO_PROXY, null)
        listener.secureConnectStart(call)
        listener.secureConnectEnd(call, null)
        listener.responseHeadersStart(call)
        listener.responseBodyEnd(call, 0L)
        listener.callFailed(call, IOException())

        val metrics = listener.buildMetrics()

        assertThat(metrics.dnsLookup).isEqualTo(10L)
        assertThat(metrics.tcpConnect).isEqualTo(10L)
        assertThat(metrics.tlsHandshake).isEqualTo(10L)
        assertThat(metrics.response).isEqualTo(10L)
    }
}

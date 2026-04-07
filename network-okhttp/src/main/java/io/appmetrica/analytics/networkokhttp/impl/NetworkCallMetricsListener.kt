package io.appmetrica.analytics.networkokhttp.impl

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.networkapi.NetworkCallMetrics
import okhttp3.Call
import okhttp3.EventListener
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy

internal class NetworkCallMetricsListener(
    private val timeProvider: TimeProvider = SystemTimeProvider(),
) : EventListener() {

    private var dnsStart: Long = 0L
    private var dnsEnd: Long = 0L
    private var connectStart: Long = 0L
    private var connectEnd: Long = 0L
    private var tlsStart: Long = 0L
    private var tlsEnd: Long = 0L
    private var requestHeadersEnd: Long = 0L
    private var responseHeadersStart: Long = 0L
    private var responseBodyEnd: Long = 0L
    private var protocol: String? = null
    private var connectionReused: Boolean = false

    override fun dnsStart(call: Call, domainName: String) {
        dnsStart = timeProvider.currentTimeMillis()
    }

    override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
        dnsEnd = timeProvider.currentTimeMillis()
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        connectStart = timeProvider.currentTimeMillis()
    }

    override fun connectEnd(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: okhttp3.Protocol?
    ) {
        connectEnd = timeProvider.currentTimeMillis()
    }

    override fun secureConnectStart(call: Call) {
        tlsStart = timeProvider.currentTimeMillis()
    }

    override fun secureConnectEnd(call: Call, handshake: okhttp3.Handshake?) {
        tlsEnd = timeProvider.currentTimeMillis()
    }

    override fun requestHeadersEnd(call: Call, request: okhttp3.Request) {
        requestHeadersEnd = timeProvider.currentTimeMillis()
    }

    override fun responseHeadersStart(call: Call) {
        responseHeadersStart = timeProvider.currentTimeMillis()
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        responseBodyEnd = timeProvider.currentTimeMillis()
    }

    override fun connectionAcquired(call: Call, connection: okhttp3.Connection) {
        connectionReused = connectStart == 0L
        protocol = connection.protocol().toString()
    }

    override fun callFailed(call: Call, ioe: IOException) {
        val failure = timeProvider.currentTimeMillis()
        if (dnsStart > 0L && dnsEnd == 0L) dnsEnd = failure
        if (connectStart > 0L && connectEnd == 0L) connectEnd = failure
        if (tlsStart > 0L && tlsEnd == 0L) tlsEnd = failure
        if (responseHeadersStart > 0L && responseBodyEnd == 0L) responseBodyEnd = failure
    }

    private fun diff(from: Long, to: Long): Long? = if (from > 0L && to > 0L) to - from else null

    fun buildMetrics(): NetworkCallMetrics {
        val dnsLookup = diff(dnsStart, dnsEnd)
        val tcpConnect = diff(connectStart, connectEnd)
        val tlsHandshake = diff(tlsStart, tlsEnd)
        val timeToFirstByte = diff(requestHeadersEnd, responseHeadersStart)
        val response = diff(responseHeadersStart, responseBodyEnd)

        return NetworkCallMetrics.Builder()
            .withDnsLookup(dnsLookup)
            .withTcpConnect(tcpConnect)
            .withTlsHandshake(tlsHandshake)
            .withTimeToFirstByte(timeToFirstByte)
            .withResponse(response)
            .withConnectionReused(connectionReused)
            .withProtocol(protocol)
            .build()
    }
}

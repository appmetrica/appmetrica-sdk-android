package io.appmetrica.analytics.networkapi

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NetworkCallMetricsTest : CommonTest() {

    @Test
    fun builderWithAllFields() {
        val metrics = NetworkCallMetrics.Builder()
            .withDnsLookup(10L)
            .withTcpConnect(20L)
            .withTlsHandshake(15L)
            .withTimeToFirstByte(30L)
            .withResponse(40L)
            .withConnectionReused(true)
            .withProtocol("h2")
            .build()

        ObjectPropertyAssertions(metrics)
            .checkField("dnsLookup", 10L)
            .checkField("tcpConnect", 20L)
            .checkField("tlsHandshake", 15L)
            .checkField("timeToFirstByte", 30L)
            .checkField("response", 40L)
            .checkField("connectionReused", true)
            .checkField("protocol", "h2")
            .checkAll()
    }

    @Test
    fun builderDefaults() {
        val metrics = NetworkCallMetrics.Builder().build()

        ObjectPropertyAssertions(metrics)
            .checkFieldIsNull("dnsLookup")
            .checkFieldIsNull("tcpConnect")
            .checkFieldIsNull("tlsHandshake")
            .checkFieldIsNull("timeToFirstByte")
            .checkFieldIsNull("response")
            .checkField("connectionReused", false)
            .checkFieldIsNull("protocol")
            .checkAll()
    }

    @Test
    fun builderWithNullableFields() {
        val metrics = NetworkCallMetrics.Builder()
            .withDnsLookup(null)
            .withTcpConnect(null)
            .withTlsHandshake(null)
            .withTimeToFirstByte(100L)
            .withResponse(50L)
            .build()

        assertThat(metrics.dnsLookup).isNull()
        assertThat(metrics.tcpConnect).isNull()
        assertThat(metrics.tlsHandshake).isNull()
        assertThat(metrics.timeToFirstByte).isEqualTo(100L)
        assertThat(metrics.response).isEqualTo(50L)
    }
}

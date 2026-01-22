package io.appmetrica.analytics.networklegacy.impl

import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class LegacyNetworkClientTest : CommonTest() {

    private val settings = NetworkClientSettings.Builder()
        .withConnectTimeout(5000)
        .withReadTimeout(10000)
        .build()

    @Test
    fun `constructor creates client with settings`() {
        val client = LegacyNetworkClient(settings)

        assertThat(client.settings).isEqualTo(settings)
    }

    @Test
    fun `newCall returns CallImpl with correct parameters`() {
        val request = Request.Builder("https://example.com").build()
        val client = LegacyNetworkClient(settings)

        val call = client.newCall(request)

        assertThat(call).isInstanceOf(CallImpl::class.java)
    }

    @Test
    fun `toString returns correct format`() {
        val client = LegacyNetworkClient(settings)

        val string = client.toString()

        assertThat(string).contains("LegacyNetworkClient")
        assertThat(string).contains("settings=")
    }
}

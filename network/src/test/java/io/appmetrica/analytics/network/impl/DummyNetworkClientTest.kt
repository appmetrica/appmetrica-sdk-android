package io.appmetrica.analytics.network.impl

import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

internal class DummyNetworkClientTest : CommonTest() {

    private val settings: NetworkClientSettings = mock()

    private val client = DummyNetworkClient(settings)

    @Test
    fun `newCall returns DummyCall`() {
        val request = Request.Builder("https://example.com").build()

        val call = client.newCall(request)

        assertThat(call).isInstanceOf(DummyCall::class.java)
    }

    @Test
    fun `getSettings returns settings`() {
        assertThat(client.settings).isEqualTo(settings)
    }

    @Test
    fun `newCall with different requests returns DummyCall instances`() {
        val request1 = Request.Builder("https://example.com/1").build()
        val request2 = Request.Builder("https://example.com/2").build()

        val call1 = client.newCall(request1)
        val call2 = client.newCall(request2)

        assertThat(call1).isInstanceOf(DummyCall::class.java)
        assertThat(call2).isInstanceOf(DummyCall::class.java)
    }

    @Test
    fun `executing call from newCall returns error response`() {
        val request = Request.Builder("https://example.com").build()
        val call = client.newCall(request)

        val response = call.execute()

        assertThat(response.isCompleted).isFalse()
        assertThat(response.exception).isInstanceOf(IllegalStateException::class.java)
    }
}

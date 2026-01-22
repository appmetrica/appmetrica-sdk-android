package io.appmetrica.analytics.networkokhttp.impl

import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request
import io.appmetrica.analytics.testutils.CommonTest
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test

internal class OkHttpNetworkClientTest : CommonTest() {

    private val settings = NetworkClientSettings.Builder()
        .withConnectTimeout(5000)
        .withReadTimeout(10000)
        .build()

    private val okHttpClient = OkHttpClient()

    @Test
    fun constructorWithSettings() {
        val client = OkHttpNetworkClient(settings)

        assertThat(client.settings).isEqualTo(settings)
    }

    @Test
    fun constructorWithSettingsAndOkHttpClient() {
        val client = OkHttpNetworkClient(settings, okHttpClient)

        assertThat(client.settings).isEqualTo(settings)
    }

    @Test
    fun newCallReturnsCallImpl() {
        val request = Request.Builder("https://example.com").build()
        val client = OkHttpNetworkClient(settings, okHttpClient)

        val call = client.newCall(request)

        assertThat(call).isInstanceOf(CallImpl::class.java)
    }

    @Test
    fun newCallWithDifferentRequests() {
        val request1 = Request.Builder("https://example1.com").build()
        val request2 = Request.Builder("https://example2.com").build()
        val client = OkHttpNetworkClient(settings, okHttpClient)

        val call1 = client.newCall(request1)
        val call2 = client.newCall(request2)

        SoftAssertions().apply {
            assertThat(call1).`as`("call1 not same as call2").isNotSameAs(call2)
            assertThat(call1).`as`("call1 is CallImpl").isInstanceOf(CallImpl::class.java)
            assertThat(call2).`as`("call2 is CallImpl").isInstanceOf(CallImpl::class.java)
            assertAll()
        }
    }

    @Test
    fun toStringContainsSettings() {
        val client = OkHttpNetworkClient(settings, okHttpClient)

        val string = client.toString()

        SoftAssertions().apply {
            assertThat(string).`as`("contains class name").contains("OkHttpNetworkClient")
            assertThat(string).`as`("contains settings").contains("settings=")
            assertAll()
        }
    }

    @Test
    fun settingsAreAccessible() {
        val customSettings = NetworkClientSettings.Builder()
            .withConnectTimeout(3000)
            .withReadTimeout(6000)
            .withMaxResponseSize(1024)
            .build()

        val client = OkHttpNetworkClient(customSettings, okHttpClient)

        SoftAssertions().apply {
            assertThat(client.settings.connectTimeout).`as`("connectTimeout").isEqualTo(3000)
            assertThat(client.settings.readTimeout).`as`("readTimeout").isEqualTo(6000)
            assertThat(client.settings.maxResponseSize).`as`("maxResponseSize").isEqualTo(1024)
            assertAll()
        }
    }
}

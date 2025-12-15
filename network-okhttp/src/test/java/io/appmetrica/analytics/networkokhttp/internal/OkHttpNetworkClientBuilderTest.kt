package io.appmetrica.analytics.networkokhttp.internal

import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkokhttp.impl.OkHttpNetworkClient
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.mockito.kotlin.mock

class OkHttpNetworkClientBuilderTest : CommonTest() {

    @Test
    fun `build with settings creates OkHttpNetworkClient`() {
        val settings: NetworkClientSettings = mock()
        val client = OkHttpNetworkClientBuilder()
            .withSettings(settings)
            .build()

        SoftAssertions().apply {
            assertThat(client).`as`("client type").isInstanceOf(OkHttpNetworkClient::class.java)
            assertThat(client.settings).`as`("settings").isSameAs(settings)
            assertAll()
        }
    }

    @Test
    fun `toString returns correct description`() {
        val builder = OkHttpNetworkClientBuilder()

        assertThat(builder.toString()).isEqualTo("OkHttp Network Client Builder")
    }
}

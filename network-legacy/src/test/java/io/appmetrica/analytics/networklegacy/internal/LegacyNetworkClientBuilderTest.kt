package io.appmetrica.analytics.networklegacy.internal

import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networklegacy.impl.LegacyNetworkClient
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.mockito.kotlin.mock

class LegacyNetworkClientBuilderTest : CommonTest() {

    @Test
    fun `build with settings creates LegacyNetworkClient`() {
        val settings: NetworkClientSettings = mock()
        val client = LegacyNetworkClientBuilder()
            .withSettings(settings).build()

        SoftAssertions().apply {
            assertThat(client).`as`("client type").isInstanceOf(LegacyNetworkClient::class.java)
            assertThat(client.settings).`as`("settings").isSameAs(settings)
            assertAll()
        }
    }

    @Test
    fun `toString returns correct description`() {
        val builder = LegacyNetworkClientBuilder()

        assertThat(builder.toString()).isEqualTo("Legacy Network Client Builder")
    }
}

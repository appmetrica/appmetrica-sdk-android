package io.appmetrica.analytics.network.internal

import io.appmetrica.analytics.network.impl.NetworkClientFactory
import io.appmetrica.analytics.networkapi.NetworkClient
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class NetworkClientBuilderTest : CommonTest() {

    private val mockClient: NetworkClient = mock()
    private val networkClientSettings: NetworkClientSettings = mock()

    private val builder = NetworkClientBuilder()

    @get:Rule
    val factoryRule = staticRule<NetworkClientFactory>()

    @Test
    fun `all with methods return same builder instance for fluent API`() {
        SoftAssertions.assertSoftly { softly ->
            softly.assertThat(builder.withSettings(networkClientSettings)).isSameAs(builder)
        }
    }

    @Test
    fun `build with all parameters passes correct values to factory`() {
        whenever(
            NetworkClientFactory.createNetworkClient(networkClientSettings)
        ).thenReturn(mockClient)

        val result = builder
            .withSettings(networkClientSettings)
            .build()

        assertThat(result).isSameAs(mockClient)
        factoryRule.staticMock.verify {
            NetworkClientFactory.createNetworkClient(networkClientSettings)
        }
    }
}

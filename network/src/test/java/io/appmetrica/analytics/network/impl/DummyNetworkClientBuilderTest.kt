package io.appmetrica.analytics.network.impl

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class DummyNetworkClientBuilderTest : CommonTest() {

    @Test
    fun `build returns DummyNetworkClient`() {
        val client = DummyNetworkClientBuilder().build()
        assertThat(client).isInstanceOf(DummyNetworkClient::class.java)
    }
}

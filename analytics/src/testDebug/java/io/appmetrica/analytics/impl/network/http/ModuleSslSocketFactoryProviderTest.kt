package io.appmetrica.analytics.impl.network.http

import io.appmetrica.analytics.coreapi.internal.io.SslConfiguration
import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.impl.startup.CollectingFlags
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

internal class ModuleSslSocketFactoryProviderTest : CommonTest() {

    @Test
    fun `snapshot stays from one delegate when delegate changes during read`() {
        val underTest = ModuleSslSocketFactoryProvider()
        val first = SslConfiguration(mock(), mock())
        val second = SslConfiguration(mock(), mock())
        val next = provider(second)
        underTest.setDelegate(provider(first) { underTest.setDelegate(next) })

        assertThat(underTest.sslConfiguration).isSameAs(first)
        assertThat(underTest.sslConfiguration).isSameAs(second)
        underTest.setDelegate(null)
        assertThat(underTest.sslConfiguration).isSameAs(SslConfiguration.EMPTY)
    }

    @Test
    fun `null delegate until set then forwards`() {
        val underTest = ModuleSslSocketFactoryProvider()
        assertThat(underTest.sslConfiguration).isSameAs(SslConfiguration.EMPTY)

        val configuration = SslConfiguration(mock(), mock())
        underTest.setDelegate(provider(configuration))

        assertThat(underTest.sslConfiguration).isSameAs(configuration)

        underTest.setDelegate(null)
        assertThat(underTest.sslConfiguration).isSameAs(SslConfiguration.EMPTY)

        underTest.onStartupStateChanged(
            StartupState.Builder(mock<CollectingFlags>()).build()
        )
    }

    private fun provider(configuration: SslConfiguration, onRead: () -> Unit = {}) =
        object : SslSocketFactoryProvider {
            override val sslConfiguration: SslConfiguration
                get() {
                    onRead()
                    return configuration
                }
        }
}

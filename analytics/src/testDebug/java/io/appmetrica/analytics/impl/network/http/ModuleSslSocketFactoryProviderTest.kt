package io.appmetrica.analytics.impl.network.http

import io.appmetrica.analytics.coreapi.internal.io.SslSocketFactoryProvider
import io.appmetrica.analytics.impl.startup.CollectingFlags
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

internal class ModuleSslSocketFactoryProviderTest : CommonTest() {

    @Test
    fun `null delegate until set then forwards`() {
        val underTest = ModuleSslSocketFactoryProvider()
        assertThat(underTest.sslSocketFactory).isNull()
        assertThat(underTest.trustManager).isNull()

        val delegate = object : SslSocketFactoryProvider {
            override val sslSocketFactory: SSLSocketFactory = mock()
            override val trustManager: X509TrustManager = mock()
        }
        underTest.setDelegate(delegate)

        assertThat(underTest.sslSocketFactory).isSameAs(delegate.sslSocketFactory)
        assertThat(underTest.trustManager).isSameAs(delegate.trustManager)

        underTest.setDelegate(null)
        assertThat(underTest.sslSocketFactory).isNull()
        assertThat(underTest.trustManager).isNull()

        underTest.onStartupStateChanged(
            StartupState.Builder(mock<CollectingFlags>()).build()
        )
    }
}

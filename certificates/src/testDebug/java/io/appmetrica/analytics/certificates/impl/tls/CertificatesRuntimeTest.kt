package io.appmetrica.analytics.certificates.impl.tls

import io.appmetrica.analytics.certificates.impl.config.service.model.ServiceSideNetworkCertificatesConfig
import io.appmetrica.analytics.certificates.testutils.CertificatesServiceContextMocks
import io.appmetrica.analytics.coreapi.internal.io.SslConfiguration
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

internal class CertificatesRuntimeTest : CommonTest() {

    private val trustManager: X509TrustManager = mock()
    private val socketFactory: SSLSocketFactory = mock()
    private val otherTrustManager: X509TrustManager = mock()
    private val otherSocketFactory: SSLSocketFactory = mock()

    @Test
    fun `rebuild publishes complete generations and keeps old snapshots unchanged`() {
        lateinit var runtime: CertificatesRuntime
        var expectedDuringBuild = SslConfiguration.EMPTY
        var nextTrustManager = trustManager
        var nextSocketFactory = socketFactory
        runtime = CertificatesRuntime(
            createTrustManager = { nextTrustManager },
            createSocketFactory = { manager ->
                assertThat(manager).isSameAs(nextTrustManager)
                assertThat(runtime.sslConfiguration).isSameAs(expectedDuringBuild)
                nextSocketFactory
            }
        )
        runtime.attach(CertificatesServiceContextMocks.serviceContext(), null)
        val first = runtime.sslConfiguration
        assertThat(first.sslSocketFactory).isSameAs(socketFactory)
        assertThat(first.trustManager).isSameAs(trustManager)

        expectedDuringBuild = first
        nextTrustManager = otherTrustManager
        nextSocketFactory = otherSocketFactory
        runtime.attach(CertificatesServiceContextMocks.serviceContext(), null)
        val second = runtime.sslConfiguration
        assertThat(second.sslSocketFactory).isSameAs(otherSocketFactory)
        assertThat(second.trustManager).isSameAs(otherTrustManager)
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = false))
        assertThat(runtime.sslConfiguration).isSameAs(SslConfiguration.EMPTY)
        assertThat(first.sslSocketFactory).isSameAs(socketFactory)
        assertThat(first.trustManager).isSameAs(trustManager)
        assertThat(second.sslSocketFactory).isSameAs(otherSocketFactory)
        assertThat(second.trustManager).isSameAs(otherTrustManager)
    }

    @Test
    fun `lifecycle skip rebuild failure and replay`() {
        var trustCalls = 0
        var factoryCalls = 0
        var nextTrust: X509TrustManager? = trustManager
        var nextFactory: () -> SSLSocketFactory = { socketFactory }
        val runtime = CertificatesRuntime(
            createTrustManager = {
                trustCalls++
                nextTrust
            },
            createSocketFactory = {
                factoryCalls++
                nextFactory()
            },
        )

        // Disabled update before attach: no context, factory not used.
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = false))
        assertThat(runtime.sslConfiguration).isSameAs(SslConfiguration.EMPTY)
        assertThat(trustCalls).isEqualTo(0)
        assertThat(factoryCalls).isEqualTo(0)

        // Enabled update before attach also keeps the empty configuration.
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = true))
        assertThat(runtime.sslConfiguration).isSameAs(SslConfiguration.EMPTY)
        assertThat(trustCalls).isEqualTo(0)
        assertThat(factoryCalls).isEqualTo(0)

        val serviceContext = CertificatesServiceContextMocks.serviceContext()
        runtime.attach(serviceContext, ServiceSideNetworkCertificatesConfig(enabled = true))
        assertConfiguration(runtime, socketFactory, trustManager)
        assertThat(trustCalls).isEqualTo(1)
        assertThat(factoryCalls).isEqualTo(1)

        // same enabled replay: identities kept, no rebuild
        val configurationAfterSuccess = runtime.sslConfiguration
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = true))
        assertThat(runtime.sslConfiguration).isSameAs(configurationAfterSuccess)
        assertThat(trustCalls).isEqualTo(1)
        assertThat(factoryCalls).isEqualTo(1)

        // disable then enable with another pair
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = false))
        assertThat(runtime.sslConfiguration).isSameAs(SslConfiguration.EMPTY)
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = false))
        assertThat(runtime.sslConfiguration).isSameAs(SslConfiguration.EMPTY)
        assertThat(trustCalls).isEqualTo(1)
        assertThat(factoryCalls).isEqualTo(1)

        nextTrust = otherTrustManager
        nextFactory = { otherSocketFactory }
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = true))
        assertConfiguration(runtime, otherSocketFactory, otherTrustManager)
        assertThat(trustCalls).isEqualTo(2)
        assertThat(factoryCalls).isEqualTo(2)

        // manager null after success clears both
        nextTrust = null
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = false))
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = true))
        assertThat(runtime.sslConfiguration).isSameAs(SslConfiguration.EMPTY)
        assertThat(trustCalls).isEqualTo(3)

        // factory failure clears both; same enabled retries
        nextTrust = trustManager
        nextFactory = { throw IllegalStateException("factory failed") }
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = true))
        assertThat(runtime.sslConfiguration).isSameAs(SslConfiguration.EMPTY)
        assertThat(trustCalls).isEqualTo(4)
        assertThat(factoryCalls).isEqualTo(3)

        nextFactory = { socketFactory }
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = true))
        assertConfiguration(runtime, socketFactory, trustManager)
        assertThat(trustCalls).isEqualTo(5)
        assertThat(factoryCalls).isEqualTo(4)
    }

    @Test
    fun `attach and updateConfig enable disable and null config paths`() {
        val runtime = CertificatesRuntime(
            createTrustManager = { trustManager },
            createSocketFactory = { socketFactory },
        )
        val serviceContext = CertificatesServiceContextMocks.serviceContext()

        runtime.attach(serviceContext, null)
        assertThat(runtime.config.enabled).isTrue()
        assertConfiguration(runtime, socketFactory, trustManager)

        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = false))
        assertThat(runtime.config.enabled).isFalse()
        assertThat(runtime.sslConfiguration).isSameAs(SslConfiguration.EMPTY)

        runtime.updateConfig(null)
        assertThat(runtime.config.enabled).isTrue()
        assertConfiguration(runtime, socketFactory, trustManager)

        runtime.attach(serviceContext, ServiceSideNetworkCertificatesConfig(enabled = false))
        assertThat(runtime.config.enabled).isFalse()
        assertThat(runtime.sslConfiguration).isSameAs(SslConfiguration.EMPTY)
    }

    @Test
    fun `ready pair rebuild failures clear both and same context attach stays idempotent`() {
        var trustCalls = 0
        var factoryCalls = 0
        var nextTrust: X509TrustManager? = trustManager
        var nextFactory: () -> SSLSocketFactory = { socketFactory }
        val runtime = CertificatesRuntime(
            createTrustManager = {
                trustCalls++
                nextTrust
            },
            createSocketFactory = {
                factoryCalls++
                nextFactory()
            },
        )
        val firstContext = CertificatesServiceContextMocks.serviceContext()
        val secondContext = CertificatesServiceContextMocks.serviceContext()
        val enabled = ServiceSideNetworkCertificatesConfig(enabled = true)

        runtime.attach(firstContext, enabled)
        assertConfiguration(runtime, socketFactory, trustManager)
        assertThat(trustCalls).isEqualTo(1)
        assertThat(factoryCalls).isEqualTo(1)

        val configurationAfterReady = runtime.sslConfiguration
        runtime.attach(firstContext, enabled)
        assertThat(runtime.sslConfiguration).isSameAs(configurationAfterReady)
        assertThat(trustCalls).isEqualTo(1)
        assertThat(factoryCalls).isEqualTo(1)

        nextTrust = null
        runtime.attach(secondContext, enabled)
        assertThat(runtime.sslConfiguration).isSameAs(SslConfiguration.EMPTY)
        assertThat(trustCalls).isEqualTo(2)
        assertThat(factoryCalls).isEqualTo(1)

        nextTrust = trustManager
        runtime.attach(secondContext, enabled)
        assertConfiguration(runtime, socketFactory, trustManager)
        assertThat(trustCalls).isEqualTo(3)
        assertThat(factoryCalls).isEqualTo(2)

        nextFactory = { throw IllegalStateException("factory failed") }
        val thirdContext = CertificatesServiceContextMocks.serviceContext()
        runtime.attach(thirdContext, enabled)
        assertThat(runtime.sslConfiguration).isSameAs(SslConfiguration.EMPTY)
        assertThat(trustCalls).isEqualTo(4)
        assertThat(factoryCalls).isEqualTo(3)

        nextFactory = { socketFactory }
        runtime.attach(thirdContext, enabled)
        assertConfiguration(runtime, socketFactory, trustManager)
        assertThat(trustCalls).isEqualTo(5)
        assertThat(factoryCalls).isEqualTo(4)
    }

    private fun assertConfiguration(
        runtime: CertificatesRuntime,
        expectedSocketFactory: SSLSocketFactory,
        expectedTrustManager: X509TrustManager,
    ) {
        val configuration = runtime.sslConfiguration
        assertThat(configuration.sslSocketFactory).isSameAs(expectedSocketFactory)
        assertThat(configuration.trustManager).isSameAs(expectedTrustManager)
    }
}

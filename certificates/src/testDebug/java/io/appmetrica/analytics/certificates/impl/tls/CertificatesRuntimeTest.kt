package io.appmetrica.analytics.certificates.impl.tls

import io.appmetrica.analytics.certificates.impl.config.service.model.ServiceSideNetworkCertificatesConfig
import io.appmetrica.analytics.certificates.testutils.CertificatesServiceContextMocks
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

        // update before attach: no context → both null, factory not used
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = true))
        assertThat(runtime.sslSocketFactory).isNull()
        assertThat(runtime.trustManager).isNull()
        assertThat(trustCalls).isEqualTo(0)
        assertThat(factoryCalls).isEqualTo(0)

        val serviceContext = CertificatesServiceContextMocks.serviceContext()
        runtime.attach(serviceContext, ServiceSideNetworkCertificatesConfig(enabled = true))
        assertThat(runtime.sslSocketFactory).isSameAs(socketFactory)
        assertThat(runtime.trustManager).isSameAs(trustManager)
        assertThat(trustCalls).isEqualTo(1)
        assertThat(factoryCalls).isEqualTo(1)

        // same enabled replay: identities kept, no rebuild
        val factoryAfterSuccess = runtime.sslSocketFactory
        val managerAfterSuccess = runtime.trustManager
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = true))
        assertThat(runtime.sslSocketFactory).isSameAs(factoryAfterSuccess)
        assertThat(runtime.trustManager).isSameAs(managerAfterSuccess)
        assertThat(trustCalls).isEqualTo(1)
        assertThat(factoryCalls).isEqualTo(1)

        // disable then enable with another pair
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = false))
        assertThat(runtime.sslSocketFactory).isNull()
        assertThat(runtime.trustManager).isNull()
        nextTrust = otherTrustManager
        nextFactory = { otherSocketFactory }
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = true))
        assertThat(runtime.sslSocketFactory).isSameAs(otherSocketFactory)
        assertThat(runtime.trustManager).isSameAs(otherTrustManager)
        assertThat(trustCalls).isEqualTo(2)
        assertThat(factoryCalls).isEqualTo(2)

        // manager null after success clears both
        nextTrust = null
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = false))
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = true))
        assertThat(runtime.sslSocketFactory).isNull()
        assertThat(runtime.trustManager).isNull()
        assertThat(trustCalls).isEqualTo(3)

        // factory failure clears both; same enabled retries
        nextTrust = trustManager
        nextFactory = { throw IllegalStateException("factory failed") }
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = true))
        assertThat(runtime.sslSocketFactory).isNull()
        assertThat(runtime.trustManager).isNull()
        assertThat(trustCalls).isEqualTo(4)
        assertThat(factoryCalls).isEqualTo(3)

        nextFactory = { socketFactory }
        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = true))
        assertThat(runtime.sslSocketFactory).isSameAs(socketFactory)
        assertThat(runtime.trustManager).isSameAs(trustManager)
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
        assertThat(runtime.sslSocketFactory).isSameAs(socketFactory)
        assertThat(runtime.trustManager).isSameAs(trustManager)

        runtime.updateConfig(ServiceSideNetworkCertificatesConfig(enabled = false))
        assertThat(runtime.config.enabled).isFalse()
        assertThat(runtime.sslSocketFactory).isNull()
        assertThat(runtime.trustManager).isNull()

        runtime.updateConfig(null)
        assertThat(runtime.config.enabled).isTrue()
        assertThat(runtime.sslSocketFactory).isSameAs(socketFactory)
        assertThat(runtime.trustManager).isSameAs(trustManager)

        runtime.attach(serviceContext, ServiceSideNetworkCertificatesConfig(enabled = false))
        assertThat(runtime.config.enabled).isFalse()
        assertThat(runtime.sslSocketFactory).isNull()
        assertThat(runtime.trustManager).isNull()
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
        assertThat(runtime.sslSocketFactory).isSameAs(socketFactory)
        assertThat(runtime.trustManager).isSameAs(trustManager)
        assertThat(trustCalls).isEqualTo(1)
        assertThat(factoryCalls).isEqualTo(1)

        val factoryAfterReady = runtime.sslSocketFactory
        val managerAfterReady = runtime.trustManager
        runtime.attach(firstContext, enabled)
        assertThat(runtime.sslSocketFactory).isSameAs(factoryAfterReady)
        assertThat(runtime.trustManager).isSameAs(managerAfterReady)
        assertThat(trustCalls).isEqualTo(1)
        assertThat(factoryCalls).isEqualTo(1)

        nextTrust = null
        runtime.attach(secondContext, enabled)
        assertThat(runtime.sslSocketFactory).isNull()
        assertThat(runtime.trustManager).isNull()
        assertThat(trustCalls).isEqualTo(2)
        assertThat(factoryCalls).isEqualTo(1)

        nextTrust = trustManager
        runtime.attach(secondContext, enabled)
        assertThat(runtime.sslSocketFactory).isSameAs(socketFactory)
        assertThat(runtime.trustManager).isSameAs(trustManager)
        assertThat(trustCalls).isEqualTo(3)
        assertThat(factoryCalls).isEqualTo(2)

        nextFactory = { throw IllegalStateException("factory failed") }
        val thirdContext = CertificatesServiceContextMocks.serviceContext()
        runtime.attach(thirdContext, enabled)
        assertThat(runtime.sslSocketFactory).isNull()
        assertThat(runtime.trustManager).isNull()
        assertThat(trustCalls).isEqualTo(4)
        assertThat(factoryCalls).isEqualTo(3)

        nextFactory = { socketFactory }
        runtime.attach(thirdContext, enabled)
        assertThat(runtime.sslSocketFactory).isSameAs(socketFactory)
        assertThat(runtime.trustManager).isSameAs(trustManager)
        assertThat(trustCalls).isEqualTo(5)
        assertThat(factoryCalls).isEqualTo(4)
    }
}

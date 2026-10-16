package io.appmetrica.analytics.certificates.internal

import io.appmetrica.analytics.certificates.impl.Constants
import io.appmetrica.analytics.certificates.impl.config.service.model.ServiceSideNetworkCertificatesConfig
import io.appmetrica.analytics.certificates.testutils.CertificatesServiceContextMocks
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class CertificatesServiceModuleEntryPointTest : CommonTest() {

    private val entryPoint by setUp { CertificatesServiceModuleEntryPoint() }

    @Test
    fun getIdentifier() {
        assertThat(entryPoint.identifier).isEqualTo(Constants.MODULE_ID)
    }

    @Test
    fun getFeatures() {
        assertThat(entryPoint.remoteConfigExtensionConfiguration.getFeatures())
            .containsExactly(Constants.RemoteConfig.FEATURE_NAME_OBFUSCATED)
    }

    @Test
    fun getBlocks() {
        assertThat(entryPoint.remoteConfigExtensionConfiguration.getBlocks()).isEmpty()
    }

    @Test
    fun `constructor does not create tls objects`() {
        assertThat(entryPoint.sslSocketFactory).isNull()
        assertThat(entryPoint.trustManager).isNull()
    }

    @Test
    fun initServiceSideAppliesConfig() {
        val config = ServiceSideNetworkCertificatesConfig(enabled = false)
        val wrapper = ServiceSideNetworkCertificatesConfigWrapper(config)
        val serviceContext: ServiceContext = mock()
        val initialConfig: ModuleRemoteConfig<ServiceSideNetworkCertificatesConfigWrapper?> = mock {
            on { featuresConfig } doReturn wrapper
        }

        entryPoint.initServiceSide(serviceContext, initialConfig)

        assertThat(entryPoint.sslSocketFactory).isNull()
        assertThat(entryPoint.trustManager).isNull()
    }

    @Test
    fun `init enables tls and remote update can disable`() {
        val serviceContext = CertificatesServiceContextMocks.serviceContext()
        val initialConfig: ModuleRemoteConfig<ServiceSideNetworkCertificatesConfigWrapper?> = mock {
            on { featuresConfig } doReturn null
        }
        entryPoint.initServiceSide(serviceContext, initialConfig)
        assertThat(entryPoint.sslSocketFactory).isNotNull()
        assertThat(entryPoint.trustManager).isNotNull()

        val updated = ServiceSideNetworkCertificatesConfig(enabled = false)
        val updatedConfig: ModuleRemoteConfig<ServiceSideNetworkCertificatesConfigWrapper?> = mock {
            on { featuresConfig } doReturn ServiceSideNetworkCertificatesConfigWrapper(updated)
        }
        entryPoint.remoteConfigExtensionConfiguration.getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(updatedConfig)

        assertThat(entryPoint.sslSocketFactory).isNull()
        assertThat(entryPoint.trustManager).isNull()
    }
}

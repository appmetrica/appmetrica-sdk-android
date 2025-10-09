package io.appmetrica.analytics.billing.internal

import io.appmetrica.analytics.billing.impl.BillingMonitorWrapper
import io.appmetrica.analytics.billing.impl.config.remote.RemoteBillingConfigConverter
import io.appmetrica.analytics.billing.impl.config.remote.RemoteBillingConfigParser
import io.appmetrica.analytics.billing.internal.config.BillingConfig
import io.appmetrica.analytics.billing.internal.config.RemoteBillingConfig
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleReporterComponentLifecycle
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class BillingServiceModuleEntryPointTest : CommonTest() {

    private val remoteConfig = RemoteBillingConfig(
        enabled = true,
        config = BillingConfig(
            sendFrequencySeconds = 42,
            firstCollectingInappMaxAgeSeconds = 4242
        )
    )
    private val serviceModuleReporterComponentLifecycle: ServiceModuleReporterComponentLifecycle = mock()
    private val serviceContext: ServiceContext = mock {
        on { serviceModuleReporterComponentLifecycle } doReturn serviceModuleReporterComponentLifecycle
    }
    private val initialConfig: ModuleRemoteConfig<RemoteBillingConfig?> = mock {
        on { featuresConfig } doReturn remoteConfig
    }

    @get:Rule
    val billingMonitorWrapperRule = constructionRule<BillingMonitorWrapper>()
    @get:Rule
    val configParserRule = constructionRule<RemoteBillingConfigParser>()
    @get:Rule
    val configConverterRule = constructionRule<RemoteBillingConfigConverter>()

    private val entryPoint by setUp { BillingServiceModuleEntryPoint() }

    @Test
    fun getIdentifier() {
        assertThat(entryPoint.identifier).isEqualTo("billing")
    }

    @Test
    fun getFeatures() {
        assertThat(entryPoint.remoteConfigExtensionConfiguration.getFeatures()).isEmpty()
    }

    @Test
    fun getBlocks() {
        assertThat(entryPoint.remoteConfigExtensionConfiguration.getBlocks()).containsExactlyEntriesOf(
            mapOf("aic" to 1)
        )
    }

    @Test
    fun getJsonParser() {
        assertThat(entryPoint.remoteConfigExtensionConfiguration.getJsonParser()).isSameAs(configParser())
    }

    @Test
    fun getProtobufConverter() {
        assertThat(entryPoint.remoteConfigExtensionConfiguration.getProtobufConverter()).isSameAs(configConverter())
    }

    @Test
    fun configUpdateListener() {
        entryPoint.initServiceSide(serviceContext, initialConfig)
        entryPoint.remoteConfigExtensionConfiguration.getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(initialConfig)

        verify(billingMonitorWrapper()).updateConfig(any())
    }

    private fun billingMonitorWrapper(): BillingMonitorWrapper {
        assertThat(billingMonitorWrapperRule.constructionMock.constructed()).hasSize(1)
        return billingMonitorWrapperRule.constructionMock.constructed().first()
    }

    private fun configParser(): RemoteBillingConfigParser {
        assertThat(configParserRule.constructionMock.constructed()).hasSize(1)
        return configParserRule.constructionMock.constructed().first()
    }

    private fun configConverter(): RemoteBillingConfigConverter {
        assertThat(configConverterRule.constructionMock.constructed()).hasSize(1)
        return configConverterRule.constructionMock.constructed().first()
    }
}

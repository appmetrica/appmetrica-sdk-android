package io.appmetrica.analytics.billing.internal

import io.appmetrica.analytics.billing.impl.BillingMonitorWrapper
import io.appmetrica.analytics.billing.impl.config.service.ServiceSideBillingConfigConverter
import io.appmetrica.analytics.billing.impl.config.service.ServiceSideBillingConfigParser
import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideBillingConfig
import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideRemoteBillingConfig
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleReporterComponentLifecycle
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class BillingServiceModuleEntryPointTest : CommonTest() {

    private val remoteConfig = ServiceSideRemoteBillingConfig(
        enabled = true,
        config = ServiceSideBillingConfig(
            sendFrequencySeconds = 42,
            firstCollectingInappMaxAgeSeconds = 4242
        )
    )
    private val wrapper = ServiceSideBillingConfigWrapper(remoteConfig)
    private val serviceModuleReporterComponentLifecycle: ServiceModuleReporterComponentLifecycle = mock()
    private val serviceContext: ServiceContext = mock {
        on { serviceModuleReporterComponentLifecycle } doReturn serviceModuleReporterComponentLifecycle
    }
    private val initialConfig: ModuleRemoteConfig<ServiceSideBillingConfigWrapper?> = mock {
        on { featuresConfig } doReturn wrapper
    }

    @get:Rule
    val billingMonitorWrapperRule = constructionRule<BillingMonitorWrapper>()
    @get:Rule
    val configParserRule = constructionRule<ServiceSideBillingConfigParser>()
    @get:Rule
    val configConverterRule = constructionRule<ServiceSideBillingConfigConverter>()

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
    fun configUpdateListener() {
        entryPoint.initServiceSide(serviceContext, initialConfig)
        entryPoint.remoteConfigExtensionConfiguration.getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(initialConfig)

        verify(billingMonitorWrapper()).updateConfig(any())
    }

    private fun billingMonitorWrapper(): BillingMonitorWrapper = billingMonitorWrapperRule.single()

    private fun configParser(): ServiceSideBillingConfigParser = configParserRule.single()
}

package io.appmetrica.analytics.adrevenue.other.internal

import io.appmetrica.analytics.adrevenue.other.impl.Constants
import io.appmetrica.analytics.adrevenue.other.impl.config.client.BundleToClientSideAdRevenueOtherConfigConverter
import io.appmetrica.analytics.adrevenue.other.impl.config.client.model.ClientSideAdRevenueOtherConfig
import io.appmetrica.analytics.adrevenue.other.impl.fb.FBAdRevenueAdapter
import io.appmetrica.analytics.adrevenue.other.impl.fb.FBConstants
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleServiceConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class AdRevenueOtherClientModuleEntryPointTest : CommonTest() {

    private val enabledConfig = ClientSideAdRevenueOtherConfig(enabled = true, includeSource = true)
    private val disabledConfig = ClientSideAdRevenueOtherConfig(enabled = false, includeSource = false)

    private val clientContext: ClientContext = mock()

    @get:Rule
    val reflectionUtilsRule = staticRule<ReflectionUtils> {
        on { ReflectionUtils.detectClassExists(FBConstants.LIBRARY_MAIN_CLASS) } doReturn true
    }

    @get:Rule
    val adapterRule = constructionRule<FBAdRevenueAdapter>()

    @get:Rule
    val bundleConverterRule = constructionRule<BundleToClientSideAdRevenueOtherConfigConverter>()

    private val entryPoint by setUp { AdRevenueOtherClientModuleEntryPoint() }
    private val adapter: FBAdRevenueAdapter by adapterRule

    @Test
    fun getIdentifier() {
        assertThat(entryPoint.identifier).isEqualTo(Constants.MODULE_ID)
    }

    @Test
    fun onActivated() {
        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig)
        entryPoint.onActivated()

        verify(adapter, atLeast(1)).registerListener(clientContext)
    }

    @Test
    fun onActivatedIfDisabled() {
        entryPoint.initClientSide(clientContext)
        updateConfig(disabledConfig)
        entryPoint.onActivated()

        verify(adapter, never()).registerListener(clientContext)
    }

    @Test
    fun onActivatedIfNoLibrary() {
        whenever(ReflectionUtils.detectClassExists(FBConstants.LIBRARY_MAIN_CLASS)).thenReturn(false)
        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig)
        entryPoint.onActivated()

        verify(adapter, never()).registerListener(clientContext)
    }

    @Test
    fun onActivatedIfNoConfig() {
        entryPoint.initClientSide(clientContext)
        entryPoint.onActivated()

        verify(adapter, never()).registerListener(clientContext)
    }

    @Test
    fun configUpdateRegistersListener() {
        entryPoint.initClientSide(clientContext)
        entryPoint.onActivated()
        updateConfig(enabledConfig)

        verify(adapter).registerListener(clientContext)
    }

    @Test
    fun configUpdateUnregistersListener() {
        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig)
        entryPoint.onActivated()
        updateConfig(disabledConfig)

        verify(adapter).unregisterListener()
    }

    @Test
    fun configUpdateFromDisabledToEnabled() {
        entryPoint.initClientSide(clientContext)
        updateConfig(disabledConfig)
        entryPoint.onActivated()
        updateConfig(enabledConfig)

        verify(adapter).registerListener(clientContext)
    }

    @Test
    fun adRevenueCollectorSourceIdentifier() {
        assertThat(entryPoint.adRevenueCollector.sourceIdentifier)
            .isEqualTo(FBConstants.AD_REVENUE_SOURCE_IDENTIFIER)
    }

    @Test
    fun adRevenueCollectorEnabledWhenConfigAndLibraryAvailable() {
        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig)
        entryPoint.onActivated()

        assertThat(entryPoint.adRevenueCollector.enabled).isTrue()
    }

    @Test
    fun adRevenueCollectorDisabledWhenNoConfig() {
        assertThat(entryPoint.adRevenueCollector.enabled).isFalse()
    }

    @Test
    fun adRevenueCollectorDisabledWhenIncludeSourceFalse() {
        val configWithoutSource = ClientSideAdRevenueOtherConfig(enabled = true, includeSource = false)
        entryPoint.initClientSide(clientContext)
        updateConfig(configWithoutSource)
        entryPoint.onActivated()

        assertThat(entryPoint.adRevenueCollector.enabled).isFalse()
    }

    @Test
    fun adRevenueCollectorDisabledAfterConfigDisabled() {
        entryPoint.initClientSide(clientContext)
        updateConfig(enabledConfig)
        entryPoint.onActivated()
        updateConfig(disabledConfig)

        assertThat(entryPoint.adRevenueCollector.enabled).isFalse()
    }

    private fun updateConfig(config: ClientSideAdRevenueOtherConfig) {
        val wrapper: ClientSideAdRevenueOtherConfigWrapper = mock {
            on { this.config } doReturn config
        }
        val moduleServiceConfig: ModuleServiceConfig<ClientSideAdRevenueOtherConfigWrapper?> = mock {
            on { featuresConfig } doReturn wrapper
        }
        entryPoint.serviceConfigExtensionConfiguration.getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig)
    }
}

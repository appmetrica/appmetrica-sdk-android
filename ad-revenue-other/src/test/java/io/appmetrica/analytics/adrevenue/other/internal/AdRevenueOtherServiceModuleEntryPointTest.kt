package io.appmetrica.analytics.adrevenue.other.internal

import io.appmetrica.analytics.adrevenue.other.impl.Constants
import io.appmetrica.analytics.adrevenue.other.impl.config.service.ServiceSideAdRevenueOtherConfigConverter
import io.appmetrica.analytics.adrevenue.other.impl.config.service.ServiceSideAdRevenueOtherConfigParser
import io.appmetrica.analytics.adrevenue.other.impl.config.service.ServiceSideAdRevenueOtherConfigToBundleConverter
import io.appmetrica.analytics.adrevenue.other.impl.config.service.model.ServiceSideAdRevenueOtherConfig
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.notNull
import org.mockito.kotlin.verify

internal class AdRevenueOtherServiceModuleEntryPointTest : CommonTest() {

    private val config: ServiceSideAdRevenueOtherConfig = mock()
    private val wrapper: ServiceSideAdRevenueOtherConfigWrapper = mock {
        on { config } doReturn config
    }
    private val serviceContext: ServiceContext = mock()
    private val initialConfig: ModuleRemoteConfig<ServiceSideAdRevenueOtherConfigWrapper?> = mock {
        on { featuresConfig } doReturn wrapper
    }

    @get:Rule
    val bundleConverterRule = constructionRule<ServiceSideAdRevenueOtherConfigToBundleConverter>()

    @get:Rule
    val configParserRule = constructionRule<ServiceSideAdRevenueOtherConfigParser>()

    @get:Rule
    val configConverterRule = constructionRule<ServiceSideAdRevenueOtherConfigConverter>()

    private val entryPoint by setUp { AdRevenueOtherServiceModuleEntryPoint() }

    @Test
    fun getIdentifier() {
        assertThat(entryPoint.identifier).isEqualTo(Constants.MODULE_ID)
    }

    @Test
    fun getFeatures() {
        assertThat(entryPoint.remoteConfigExtensionConfiguration.getFeatures()).containsExactly(
            Constants.RemoteConfig.FEATURE_NAME_OBFUSCATED,
            Constants.RemoteConfig.INCLUDE_SOURCE_NAME_OBFUSCATED,
        )
    }

    @Test
    fun getBlocks() {
        assertThat(entryPoint.remoteConfigExtensionConfiguration.getBlocks()).isEmpty()
    }

    @Test
    fun getJsonParser() {
        assertThat(entryPoint.remoteConfigExtensionConfiguration.getJsonParser()).isSameAs(configParser())
    }

    @Test
    fun configToBundleBeforeInit() {
        entryPoint.clientConfigProvider.getConfigBundleForClient()
        verify(bundleConverter()).convert(null)
    }

    @Test
    fun configToBundleAfterInit() {
        entryPoint.initServiceSide(serviceContext, initialConfig)
        entryPoint.clientConfigProvider.getConfigBundleForClient()
        verify(bundleConverter()).convert(notNull())
    }

    @Test
    fun configUpdateListener() {
        entryPoint.remoteConfigExtensionConfiguration.getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(initialConfig)
        entryPoint.clientConfigProvider.getConfigBundleForClient()
        verify(bundleConverter()).convert(notNull())
    }

    private fun bundleConverter(): ServiceSideAdRevenueOtherConfigToBundleConverter {
        assertThat(bundleConverterRule.constructionMock.constructed()).hasSize(1)
        assertThat(bundleConverterRule.argumentInterceptor.flatArguments()).isEmpty()
        return bundleConverterRule.constructionMock.constructed().first()
    }

    private fun configParser(): ServiceSideAdRevenueOtherConfigParser {
        assertThat(configParserRule.constructionMock.constructed()).hasSize(1)
        return configParserRule.constructionMock.constructed().first()
    }
}

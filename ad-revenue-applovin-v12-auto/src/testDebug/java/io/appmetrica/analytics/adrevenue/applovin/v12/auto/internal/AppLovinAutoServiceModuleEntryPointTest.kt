package io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.Constants
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.ServiceApplovinConfigConverter
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.ServiceApplovinConfigParser
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.ServiceApplovinConfigToBundleConverter
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.model.ServiceApplovinConfig
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
import org.mockito.kotlin.whenever

internal class AppLovinAutoServiceModuleEntryPointTest : CommonTest() {

    private val config: ServiceApplovinConfig = mock()
    private val wrapper: ServiceApplovinConfigWrapper = mock {
        on { config } doReturn config
    }
    private val serviceContext: ServiceContext = mock()
    private val initialConfig: ModuleRemoteConfig<ServiceApplovinConfigWrapper?> = mock {
        on { featuresConfig } doReturn wrapper
    }

    @get:Rule
    val bundleConverterRule = constructionRule<ServiceApplovinConfigToBundleConverter>()

    @get:Rule
    val configParserRule = constructionRule<ServiceApplovinConfigParser>()

    @get:Rule
    val configConverterRule = constructionRule<ServiceApplovinConfigConverter>()

    private val entryPoint by setUp { AppLovinAutoServiceModuleEntryPoint() }

    @Test
    fun getIdentifier() {
        assertThat(entryPoint.identifier).isEqualTo(Constants.MODULE_ID)
    }

    @Test
    fun getFeatures() {
        assertThat(entryPoint.remoteConfigExtensionConfiguration.getFeatures()).containsExactly(
            Constants.RemoteConfig.FEATURE_NAME_OBFUSCATED,
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
    fun getProtobufConverter() {
        assertThat(entryPoint.remoteConfigExtensionConfiguration.getProtobufConverter()).isSameAs(configConverter())
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
    fun configToBundleAfterInitIfFeatureConfigIsNull() {
        whenever(initialConfig.featuresConfig).doReturn(null)
        entryPoint.initServiceSide(serviceContext, initialConfig)
        entryPoint.clientConfigProvider.getConfigBundleForClient()
        verify(bundleConverter()).convert(null)
    }

    @Test
    fun configUpdateListener() {
        entryPoint.remoteConfigExtensionConfiguration.getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(initialConfig)
        entryPoint.clientConfigProvider.getConfigBundleForClient()
        verify(bundleConverter()).convert(notNull())
    }

    @Test
    fun configUpdateListenerIfFeatureConfigIsNull() {
        whenever(initialConfig.featuresConfig).doReturn(null)
        entryPoint.remoteConfigExtensionConfiguration.getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(initialConfig)
        entryPoint.clientConfigProvider.getConfigBundleForClient()
        verify(bundleConverter()).convert(null)
    }

    private fun bundleConverter(): ServiceApplovinConfigToBundleConverter = bundleConverterRule.singleWithArgs()

    private fun configParser(): ServiceApplovinConfigParser = configParserRule.single()

    private fun configConverter(): ServiceApplovinConfigConverter = configConverterRule.single()
}

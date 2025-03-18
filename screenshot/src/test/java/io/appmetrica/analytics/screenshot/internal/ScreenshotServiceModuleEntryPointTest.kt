package io.appmetrica.analytics.screenshot.internal

import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.screenshot.impl.ServiceToBundleScreenshotConfigConverter
import io.appmetrica.analytics.screenshot.impl.config.remote.RemoteScreenshotConfigConverter
import io.appmetrica.analytics.screenshot.impl.config.remote.RemoteScreenshotConfigParser
import io.appmetrica.analytics.screenshot.impl.config.remote.model.RemoteScreenshotConfig
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.notNull
import org.mockito.kotlin.verify

class ScreenshotServiceModuleEntryPointTest : CommonTest() {

    private val remoteConfig: RemoteScreenshotConfig = mock()
    private val serviceContext: ServiceContext = mock()
    private val initialConfig: ModuleRemoteConfig<RemoteScreenshotConfig?> = mock {
        on { featuresConfig } doReturn remoteConfig
    }

    @get:Rule
    val bundleConverterRule = constructionRule<ServiceToBundleScreenshotConfigConverter>()
    @get:Rule
    val configParserRule = constructionRule<RemoteScreenshotConfigParser>()
    @get:Rule
    val configConverterRule = constructionRule<RemoteScreenshotConfigConverter>()

    private val entryPoint by setUp { ScreenshotServiceModuleEntryPoint() }

    @Test
    fun configToBundleBeforeInit() {
        entryPoint.clientConfigProvider.getConfigBundleForClient()

        verify(bundleConverter()).convert(null)
    }

    @Test
    fun configToBundleAfterInit() {
        entryPoint.initServiceSide(
            serviceContext,
            initialConfig
        )
        entryPoint.clientConfigProvider.getConfigBundleForClient()

        verify(bundleConverter()).convert(notNull())
    }

    @Test
    fun getIdentifier() {
        assertThat(entryPoint.identifier).isEqualTo("screenshot")
    }

    @Test
    fun getFeatures() {
        assertThat(entryPoint.remoteConfigExtensionConfiguration.getFeatures()).containsExactly("scr")
    }

    @Test
    fun getBlocks() {
        assertThat(entryPoint.remoteConfigExtensionConfiguration.getBlocks()).containsExactlyEntriesOf(
            mapOf("scr" to 1)
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
        entryPoint.remoteConfigExtensionConfiguration.getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(initialConfig)
        entryPoint.clientConfigProvider.getConfigBundleForClient()

        verify(bundleConverter()).convert(notNull())
    }

    private fun bundleConverter(): ServiceToBundleScreenshotConfigConverter {
        assertThat(bundleConverterRule.constructionMock.constructed()).hasSize(1)
        assertThat(bundleConverterRule.argumentInterceptor.flatArguments()).isEmpty()
        return bundleConverterRule.constructionMock.constructed().first()
    }

    private fun configParser(): RemoteScreenshotConfigParser {
        assertThat(configParserRule.constructionMock.constructed()).hasSize(1)
        return configParserRule.constructionMock.constructed().first()
    }

    private fun configConverter(): RemoteScreenshotConfigConverter {
        assertThat(configConverterRule.constructionMock.constructed()).hasSize(1)
        return configConverterRule.constructionMock.constructed().first()
    }
}

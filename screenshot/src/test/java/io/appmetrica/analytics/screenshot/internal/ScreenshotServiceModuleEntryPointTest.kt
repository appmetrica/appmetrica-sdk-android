package io.appmetrica.analytics.screenshot.internal

import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext
import io.appmetrica.analytics.screenshot.impl.config.service.ServiceSideScreenshotConfigConverter
import io.appmetrica.analytics.screenshot.impl.config.service.ServiceSideScreenshotConfigParser
import io.appmetrica.analytics.screenshot.impl.config.service.ServiceSideScreenshotConfigToBundleConverter
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideScreenshotConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.notNull
import org.mockito.kotlin.verify

internal class ScreenshotServiceModuleEntryPointTest : CommonTest() {

    private val screenshotConfig: ServiceSideScreenshotConfig = mock()
    private val wrapper: ServiceSideScreenshotConfigWrapper = mock {
        on { config } doReturn screenshotConfig
    }
    private val serviceContext: ServiceContext = mock()
    private val initialConfig: ModuleRemoteConfig<ServiceSideScreenshotConfigWrapper?> = mock {
        on { featuresConfig } doReturn wrapper
    }

    @get:Rule
    val bundleConverterRule = constructionRule<ServiceSideScreenshotConfigToBundleConverter>()
    @get:Rule
    val configParserRule = constructionRule<ServiceSideScreenshotConfigParser>()
    @get:Rule
    val configConverterRule = constructionRule<ServiceSideScreenshotConfigConverter>()

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
    fun configUpdateListener() {
        entryPoint.remoteConfigExtensionConfiguration.getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(initialConfig)
        entryPoint.clientConfigProvider.getConfigBundleForClient()

        verify(bundleConverter()).convert(notNull())
    }

    private fun bundleConverter(): ServiceSideScreenshotConfigToBundleConverter {
        assertThat(bundleConverterRule.constructionMock.constructed()).hasSize(1)
        assertThat(bundleConverterRule.argumentInterceptor.flatArguments()).isEmpty()
        return bundleConverterRule.constructionMock.constructed().first()
    }

    private fun configParser(): ServiceSideScreenshotConfigParser {
        assertThat(configParserRule.constructionMock.constructed()).hasSize(1)
        return configParserRule.constructionMock.constructed().first()
    }
}

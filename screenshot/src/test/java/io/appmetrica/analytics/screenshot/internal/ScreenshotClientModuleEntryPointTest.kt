package io.appmetrica.analytics.screenshot.internal

import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleServiceConfig
import io.appmetrica.analytics.screenshot.impl.ScreenshotCaptorsController
import io.appmetrica.analytics.screenshot.impl.config.client.BundleToClientSideScreenshotConfigConverter
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ScreenshotClientModuleEntryPointTest : CommonTest() {

    private val clientSideScreenshotConfig: ClientSideScreenshotConfig = mock()
    private val wrapper: ClientSideScreenshotConfigWrapper = mock {
        on { config } doReturn clientSideScreenshotConfig
    }
    private val moduleServiceConfig: ModuleServiceConfig<ClientSideScreenshotConfigWrapper?> = mock {
        on { featuresConfig } doReturn wrapper
    }
    private val clientContext: ClientContext = mock()

    @get:Rule
    val bundleToClientSideScreenshotConfigConverterRule =
        constructionRule<BundleToClientSideScreenshotConfigConverter>()
    @get:Rule
    val screenshotCaptorsControllerRule = constructionRule<ScreenshotCaptorsController>()

    private val entryPoint by setUp { ScreenshotClientModuleEntryPoint() }

    @Test
    fun getIdentifier() {
        assertThat(entryPoint.identifier).isEqualTo("screenshot")
    }

    @Test
    fun getBundleConverter() {
        assertThat(entryPoint.serviceConfigExtensionConfiguration.getBundleConverter()).isSameAs(
            bundleConverter()
        )
    }

    @Test
    fun initClientSide() {
        entryPoint.initClientSide(clientContext)
        assertThat(screenshotCaptorsController()).isNotNull()
    }

    @Test
    fun onActivatedIfNoInit() {
        entryPoint.onActivated()
        assertThat(screenshotCaptorsControllerRule.constructionMock.constructed()).isEmpty()
    }

    @Test
    fun onActivatedIfNoConfig() {
        entryPoint.initClientSide(clientContext)
        entryPoint.onActivated()

        verify(screenshotCaptorsController()).startCapture(null)
    }

    @Test
    fun onActivatedIfHasConfig() {
        entryPoint.initClientSide(clientContext)
        entryPoint.serviceConfigExtensionConfiguration.getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig)
        entryPoint.onActivated()

        verify(screenshotCaptorsController()).updateConfig(clientSideScreenshotConfig)
        verify(screenshotCaptorsController()).startCapture(clientSideScreenshotConfig)
    }

    @Test
    fun configUpdateListener() {
        entryPoint.initClientSide(clientContext)
        entryPoint.serviceConfigExtensionConfiguration.getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig)

        verify(screenshotCaptorsController()).updateConfig(clientSideScreenshotConfig)
    }

    @Test
    fun configUpdateListenerIfNewConfigIsNull() {
        whenever(moduleServiceConfig.featuresConfig).thenReturn(null)

        entryPoint.initClientSide(clientContext)
        entryPoint.serviceConfigExtensionConfiguration.getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig)

        verify(screenshotCaptorsController()).updateConfig(null)
    }

    private fun bundleConverter(): BundleToClientSideScreenshotConfigConverter {
        assertThat(bundleToClientSideScreenshotConfigConverterRule.constructionMock.constructed()).hasSize(1)
        assertThat(bundleToClientSideScreenshotConfigConverterRule.argumentInterceptor.flatArguments()).isEmpty()
        return bundleToClientSideScreenshotConfigConverterRule.constructionMock.constructed().first()
    }

    private fun screenshotCaptorsController(): ScreenshotCaptorsController {
        assertThat(screenshotCaptorsControllerRule.constructionMock.constructed()).hasSize(1)
        return screenshotCaptorsControllerRule.constructionMock.constructed().first()
    }
}

package io.appmetrica.analytics.screenshot.internal

import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.ModuleServiceConfig
import io.appmetrica.analytics.screenshot.impl.BundleToClientScreenshotConfigConverter
import io.appmetrica.analytics.screenshot.impl.ScreenshotCaptorsController
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideRemoteScreenshotConfig
import io.appmetrica.analytics.screenshot.internal.config.ParcelableRemoteScreenshotConfig
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ScreenshotClientModuleEntryPointTest : CommonTest() {

    private val parcelableConfig: ParcelableRemoteScreenshotConfig = mock()
    private val moduleServiceConfig: ModuleServiceConfig<ParcelableRemoteScreenshotConfig?> = mock {
        on { featuresConfig } doReturn parcelableConfig
    }
    private val clientContext: ClientContext = mock()

    @get:Rule
    val bundleToClientScreenshotConfigConverterRule = constructionRule<BundleToClientScreenshotConfigConverter>()
    @get:Rule
    val screenshotCaptorsControllerRule = constructionRule<ScreenshotCaptorsController>()
    @get:Rule
    val clientSideRemoteScreenshotConfigRule = constructionRule<ClientSideRemoteScreenshotConfig>()

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

        assertThat(clientSideRemoteScreenshotConfigRule.constructionMock.constructed()).hasSize(1)
        val newConfig = clientSideRemoteScreenshotConfigRule.constructionMock.constructed().first()
        verify(screenshotCaptorsController()).updateConfig(newConfig)
        verify(screenshotCaptorsController()).startCapture(newConfig)
    }

    @Test
    fun configUpdateListener() {
        entryPoint.initClientSide(clientContext)
        entryPoint.serviceConfigExtensionConfiguration.getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig)

        assertThat(clientSideRemoteScreenshotConfigRule.constructionMock.constructed()).hasSize(1)
        val newConfig = clientSideRemoteScreenshotConfigRule.constructionMock.constructed().first()
        verify(screenshotCaptorsController()).updateConfig(newConfig)
    }

    @Test
    fun configUpdateListenerIfNewConfigIsNull() {
        whenever(moduleServiceConfig.featuresConfig).thenReturn(null)

        entryPoint.initClientSide(clientContext)
        entryPoint.serviceConfigExtensionConfiguration.getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig)

        assertThat(clientSideRemoteScreenshotConfigRule.constructionMock.constructed()).isEmpty()
        verify(screenshotCaptorsController()).updateConfig(null)
    }

    private fun bundleConverter(): BundleToClientScreenshotConfigConverter {
        assertThat(bundleToClientScreenshotConfigConverterRule.constructionMock.constructed()).hasSize(1)
        assertThat(bundleToClientScreenshotConfigConverterRule.argumentInterceptor.flatArguments()).isEmpty()
        return bundleToClientScreenshotConfigConverterRule.constructionMock.constructed().first()
    }

    private fun screenshotCaptorsController(): ScreenshotCaptorsController {
        assertThat(screenshotCaptorsControllerRule.constructionMock.constructed()).hasSize(1)
        return screenshotCaptorsControllerRule.constructionMock.constructed().first()
    }
}

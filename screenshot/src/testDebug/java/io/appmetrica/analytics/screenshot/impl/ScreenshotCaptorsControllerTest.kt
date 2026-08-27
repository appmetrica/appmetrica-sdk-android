package io.appmetrica.analytics.screenshot.impl

import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.screenshot.impl.callback.ScreenshotCaptorCallback
import io.appmetrica.analytics.screenshot.impl.captor.CaptorProvider
import io.appmetrica.analytics.screenshot.impl.captor.ScreenshotCaptor
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig
import io.appmetrica.gradle.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ScreenshotCaptorsControllerTest : CommonTest() {

    private val config: ClientSideScreenshotConfig = mock {
        on { enabled } doReturn true
    }

    private val clientContext: ClientContext = mock()
    private val callback: ScreenshotCaptorCallback = mock()
    private val captors = listOf(
        mock<ScreenshotCaptor>(),
        mock<ScreenshotCaptor>()
    )
    private val captorProvider = mock<CaptorProvider> {
        on { getCaptors() } doReturn captors
    }

    private val screenshotCaptorsController = ScreenshotCaptorsController(clientContext, callback, captorProvider)

    @Test
    fun startCapture() {
        screenshotCaptorsController.startCapture(config)

        captors.forEach { captor ->
            verify(captor).startCapture()
            verify(captor).updateConfig(config)
        }
    }

    @Test
    fun startCaptureIfNoConfig() {
        screenshotCaptorsController.startCapture(null)

        captors.forEach { captor ->
            verify(captor).startCapture()
            verify(captor).updateConfig(null)
        }
    }

    @Test
    fun startCaptureIfConfigIsDisabled() {
        whenever(config.enabled).thenReturn(false)

        screenshotCaptorsController.startCapture(config)

        captors.forEach { captor ->
            verify(captor).startCapture()
            verify(captor).updateConfig(null)
        }
    }

    @Test
    fun updateConfig() {
        screenshotCaptorsController.updateConfig(config)

        captors.forEach { captor ->
            verify(captor).updateConfig(config)
        }
    }

    @Test
    fun updateConfigIfNoConfig() {
        screenshotCaptorsController.updateConfig(null)

        captors.forEach { captor ->
            verify(captor).updateConfig(null)
        }
    }

    @Test
    fun updateConfigIfConfigIsDisabled() {
        whenever(config.enabled).thenReturn(false)

        screenshotCaptorsController.updateConfig(config)

        captors.forEach { captor ->
            verify(captor).updateConfig(null)
        }
    }
}

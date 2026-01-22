package io.appmetrica.analytics.screenshot.impl.callback

import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.common.InternalClientModuleFacade
import io.appmetrica.analytics.modulesapi.internal.common.InternalModuleEvent
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class DefaultScreenshotCaptorCallbackTest : CommonTest() {

    private val captorType = "test"

    private val internalClientModuleFacade: InternalClientModuleFacade = mock()
    private val clientContext: ClientContext = mock {
        on { internalClientModuleFacade } doReturn internalClientModuleFacade
    }

    private val internalModuleEventCaptor = argumentCaptor<InternalModuleEvent>()

    private val defaultScreenshotCaptorCallback = DefaultScreenshotCaptorCallback(clientContext)

    @Test
    fun screenshotCaptured() {
        defaultScreenshotCaptorCallback.screenshotCaptured(captorType)

        verify(internalClientModuleFacade).reportEvent(internalModuleEventCaptor.capture())

        val internalModuleEvent = internalModuleEventCaptor.firstValue
        assertThat(internalModuleEvent.type).isEqualTo(4)
        assertThat(internalModuleEvent.name).isEqualTo("appmetrica_system_event_screenshot")
        assertThat(internalModuleEvent.getAttributes()).isEqualTo(mapOf("type" to captorType))
    }
}

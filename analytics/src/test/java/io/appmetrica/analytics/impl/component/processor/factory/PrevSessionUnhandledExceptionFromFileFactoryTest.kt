package io.appmetrica.analytics.impl.component.processor.factory

import io.appmetrica.analytics.impl.component.processor.event.ReportComponentHandler
import io.appmetrica.analytics.impl.component.processor.event.ReportCrashMetaInformation
import io.appmetrica.analytics.impl.component.processor.event.ReportPrevSessionEventHandler
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class PrevSessionUnhandledExceptionFromFileFactoryTest : CommonTest() {

    private val reportPrevSessionHandler: ReportPrevSessionEventHandler = mock()
    private val reportCrashMetaInfoHandler: ReportCrashMetaInformation = mock()

    private val provider: ReportingHandlerProvider = mock {
        on { reportPrevSessionEventHandler } doReturn reportPrevSessionHandler
        on { reportCrashMetaInformation } doReturn reportCrashMetaInfoHandler
    }

    private val factory: PrevSessionUnhandledExceptionFromFileFactory by setUp {
        PrevSessionUnhandledExceptionFromFileFactory(provider)
    }

    @Test
    fun addHandler() {
        val handlers = mutableListOf<ReportComponentHandler>()
        factory.addHandlers(handlers)

        assertThat(handlers).containsExactly(reportCrashMetaInfoHandler, reportPrevSessionHandler)
    }

}

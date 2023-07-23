/*
 * Version for Android
 * © 2019
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://yandex.com/legal/appmetrica_sdk_agreement/
 */
package io.appmetrica.analytics.impl.component.processor.event

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.component.EventSaver
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReportPrevSessionNativeCrashHandlerTest : CommonTest() {
    private val eventSaver = mock<EventSaver>()
    private val componentUnit = mock<ComponentUnit> {
        on { eventSaver } doReturn eventSaver
    }
    private val report = mock<CounterReport>()

    private val handler by setUp { ReportPrevSessionNativeCrashHandler(componentUnit) }

    @Test
    fun testProcess() {
        handler.process(report)
        verify(eventSaver).saveReportFromPrevSession(report)
    }
}

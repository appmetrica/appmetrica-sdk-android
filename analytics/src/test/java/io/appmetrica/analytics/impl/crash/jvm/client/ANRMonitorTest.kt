package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class ANRMonitorTest : CommonTest() {

    private val defaultListener = mock<ANRMonitor.Listener>()

    private val anrMonitor by setUp { ANRMonitor(defaultListener) }

    @Test
    fun `anrTicksCount with less default`() {
        val monitor = ANRMonitor(defaultListener)
        monitor.startMonitoring(2)
        checkAnrTicksCount(monitor, 5)
    }

    @Test
    fun `anrTicksCount with custom`() {
        val monitor = ANRMonitor(defaultListener)
        monitor.startMonitoring(10)
        checkAnrTicksCount(monitor, 10)
    }

    @Test
    fun `notify default listener`() {
        anrMonitor.handleAppNotResponding()
        verify(defaultListener).onAppNotResponding()
    }

    @Test
    fun `notify all subscribes`() {
        val listener1 = mock<ANRMonitor.Listener>()
        val listener2 = mock<ANRMonitor.Listener>()

        anrMonitor.subscribe(listener1)
        anrMonitor.subscribe(listener2)

        anrMonitor.handleAppNotResponding()
        verify(defaultListener).onAppNotResponding()
        verify(listener1).onAppNotResponding()
        verify(listener2).onAppNotResponding()
    }

    private fun checkAnrTicksCount(monitor: ANRMonitor, expected: Int) {
        ObjectPropertyAssertions(monitor)
            .withPrivateFields(true)
            .withIgnoredFields("listeners", "monitorThread", "completed", "uiHandler", "uiRunnable")
            .checkFieldComparingFieldByField("anrTicksCount", AtomicInteger(expected))
            .checkAll()
    }
}

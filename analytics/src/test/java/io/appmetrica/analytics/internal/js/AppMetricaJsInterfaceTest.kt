package io.appmetrica.analytics.internal.js

import io.appmetrica.analytics.impl.proxy.AppMetricaProxy
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AppMetricaJsInterfaceTest : CommonTest() {

    private val proxy: AppMetricaProxy = mock()
    private lateinit var appMetricaJsInterface: AppMetricaJsInterface

    @Before
    fun setUp() {
        appMetricaJsInterface = AppMetricaJsInterface(proxy)
    }

    @Test
    fun reportEvent() {
        val name = "event name"
        val value = "event value"
        appMetricaJsInterface.reportEvent(name, value)
        verify(proxy).reportJsEvent(name, value)
    }
}

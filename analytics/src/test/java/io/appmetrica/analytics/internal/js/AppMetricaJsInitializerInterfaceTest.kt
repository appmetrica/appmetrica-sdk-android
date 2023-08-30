package io.appmetrica.analytics.internal.js

import io.appmetrica.analytics.impl.proxy.AppMetricaProxy
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AppMetricaJsInitializerInterfaceTest : CommonTest() {

    private val proxy = mock<AppMetricaProxy>()
    private lateinit var appMetricaInitializerJsInterface: AppMetricaInitializerJsInterface

    @Before
    fun setUp() {
        appMetricaInitializerJsInterface = AppMetricaInitializerJsInterface(proxy)
    }

    @Test
    fun init() {
        val value = "event value"
        appMetricaInitializerJsInterface.init(value)
        verify(proxy).reportJsInitEvent(value)
    }
}

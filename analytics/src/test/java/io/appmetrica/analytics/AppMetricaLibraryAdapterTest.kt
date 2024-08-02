package io.appmetrica.analytics

import android.content.Context
import io.appmetrica.analytics.impl.proxy.AppMetricaLibraryAdapterProxy
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AppMetricaLibraryAdapterTest : CommonTest() {

    private val context: Context = mock()
    private val proxy: AppMetricaLibraryAdapterProxy = mock()

    @Before
    fun setUp() {
        AppMetricaLibraryAdapter.setProxy(proxy)
    }

    @Test
    fun activate() {
        AppMetricaLibraryAdapter.activate(context)
        verify(proxy).activate(context)
    }

    @Test
    fun reportEvent() {
        val sender = "sender_value"
        val event = "event_value"
        val payload = "payload_value"
        AppMetricaLibraryAdapter.reportEvent(sender, event, payload)
        verify(proxy).reportEvent(sender, event, payload)
    }
}

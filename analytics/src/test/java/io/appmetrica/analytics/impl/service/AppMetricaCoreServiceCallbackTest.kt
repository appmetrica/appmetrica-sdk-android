package io.appmetrica.analytics.impl.service

import android.app.Service
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class AppMetricaCoreServiceCallbackTest : CommonTest() {

    private val service: Service = mock()

    private val callback by setUp { AppMetricaCoreServiceCallback(service) }

    @Test
    fun onStartFinished() {
        callback.onStartFinished(100500)
        verify(service).stopSelf(100500)
    }
}

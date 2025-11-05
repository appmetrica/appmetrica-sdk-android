package io.appmetrica.analytics.impl.service

import android.os.Bundle
import io.appmetrica.analytics.impl.AppMetricaServiceCore
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AppMetricaServiceBinderTest : CommonTest() {

    private val serviceCore: AppMetricaServiceCore = mock()
    private val bundle: Bundle = mock()
    private val type = 100500

    private val binder by setUp { AppMetricaServiceBinder(serviceCore) }

    @Test
    fun resumeUserSession() {
        binder.resumeUserSession(bundle)
        verify(serviceCore).resumeUserSession(bundle)
    }

    @Test
    fun pauseUserSession() {
        binder.pauseUserSession(bundle)
        verify(serviceCore).pauseUserSession(bundle)
    }

    @Test
    fun reportData() {
        binder.reportData(type, bundle)
        verify(serviceCore).reportData(type, bundle)
    }
}

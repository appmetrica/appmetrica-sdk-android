package io.appmetrica.analytics.impl.service

import android.os.Bundle
import io.appmetrica.analytics.impl.AppMetricaServiceCoreImpl
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class AppAppMetricaServiceDataReporterTest : CommonTest() {

    private val metricaCore = mock<AppMetricaServiceCoreImpl>()
    private val appMetricaServiceDataReporter = AppMetricaServiceDataReporter(metricaCore)

    @Test
    fun reportData() {
        val type = 0
        val bundle = mock<Bundle>()

        appMetricaServiceDataReporter.reportData(type, bundle)

        verify(metricaCore).reportData(bundle)
    }
}

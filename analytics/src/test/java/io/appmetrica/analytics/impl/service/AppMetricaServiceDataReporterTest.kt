package io.appmetrica.analytics.impl.service

import android.os.Bundle
import io.appmetrica.analytics.impl.AppAppMetricaServiceCoreImpl
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AppMetricaServiceDataReporterTest : CommonTest() {

    private val metricaCore = mock<AppAppMetricaServiceCoreImpl>()
    private val metricaServiceDataReporter = MetricaServiceDataReporter(metricaCore)

    @Test
    fun reportData() {
        val type = 0
        val bundle = mock<Bundle>()

        metricaServiceDataReporter.reportData(type, bundle)

        verify(metricaCore).reportData(bundle)
    }
}

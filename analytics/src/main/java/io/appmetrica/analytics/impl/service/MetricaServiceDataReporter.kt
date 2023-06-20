package io.appmetrica.analytics.impl.service

import android.os.Bundle
import androidx.annotation.WorkerThread
import io.appmetrica.analytics.impl.MetricaCoreReporter

class MetricaServiceDataReporter(
    private val metricaCore: MetricaCoreReporter
) : ServiceDataReporter {

    @WorkerThread
    override fun reportData(type: Int, bundle: Bundle) {
        metricaCore.reportData(bundle)
    }

    companion object {

        const val TYPE_CORE = 1
    }
}

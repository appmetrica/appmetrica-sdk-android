package io.appmetrica.analytics.impl.service

import android.os.Bundle
import androidx.annotation.WorkerThread
import io.appmetrica.analytics.impl.AppMetricaCoreReporter

class AppMetricaServiceDataReporter(
    private val reporter: AppMetricaCoreReporter
) : ServiceDataReporter {

    @WorkerThread
    override fun reportData(type: Int, bundle: Bundle) {
        reporter.reportData(bundle)
    }

    companion object {

        const val TYPE_CORE = 1
    }
}

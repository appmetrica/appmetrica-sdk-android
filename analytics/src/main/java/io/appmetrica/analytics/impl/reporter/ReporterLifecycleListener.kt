package io.appmetrica.analytics.impl.reporter

import androidx.annotation.WorkerThread
import io.appmetrica.analytics.IReporter
import io.appmetrica.analytics.impl.MainReporter

internal interface ReporterLifecycleListener {

    @WorkerThread
    fun onCreateMainReporter(reporterContext: MainReporterContext, reporter: MainReporter) {
        // do nothing
    }

    @WorkerThread
    fun onCreateManualReporter(apiKey: String, reporterContext: ManualReporterContext, reporter: IReporter) {
        // do nothing
    }

    @WorkerThread
    fun onCreateCrashReporter(reporterContext: CrashReporterContext) {
        // do nothing
    }
}

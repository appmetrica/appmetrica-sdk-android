package io.appmetrica.analytics.impl.reporter

import io.appmetrica.analytics.IReporter
import io.appmetrica.analytics.impl.MainReporter

internal interface ReporterLifecycleListener {

    fun onCreateMainReporter(reporterContext: MainReporterContext, reporter: MainReporter) {
        // do nothing
    }

    fun onCreateManualReporter(apiKey: String, reporterContext: ManualReporterContext, reporter: IReporter) {
        // do nothing
    }

    fun onCreateCrashReporter(reporterContext: CrashReporterContext) {
        // do nothing
    }
}

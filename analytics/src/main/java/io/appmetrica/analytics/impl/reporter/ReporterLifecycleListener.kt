package io.appmetrica.analytics.impl.reporter

internal interface ReporterLifecycleListener {

    fun onCreateMainReporter(reporterContext: MainReporterContext) {
        // do nothing
    }

    fun onCreateManualReporter(apiKey: String, reporterContext: ManualReporterContext) {
        // do nothing
    }

    fun onCreateCrashReporter(reporterContext: CrashReporterContext) {
        // do nothing
    }
}

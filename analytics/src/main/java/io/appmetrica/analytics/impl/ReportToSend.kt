package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.ClientCounterReport.TrimmedField
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.service.MetricaServiceDataReporter
import io.appmetrica.analytics.internal.CounterConfiguration

class ReportToSend(
    val report: CounterReport,
    val isCrashReport: Boolean,
    val metricaServiceDataReporterType: Int,
    val trimmedFields: HashMap<TrimmedField, Int>?,
    val environment: ReporterEnvironment
) {

    override fun toString(): String {
        return "ReportToSend(" +
            "report=$report, " +
            "metricaServiceDataReporterType=$metricaServiceDataReporterType, " +
            "environment=$environment, " +
            "isCrashReport=$isCrashReport, " +
            "trimmedFields=$trimmedFields" +
            ")"
    }

    companion object {

        @JvmStatic
        fun newBuilder(report: CounterReport, environment: ReporterEnvironment) =
            Builder(report, environment)
    }

    class Builder(
        private val report: CounterReport,
        private val environment: ReporterEnvironment
    ) {

        private var isCrashReport = false
        private var metricaServiceDataReporterType = MetricaServiceDataReporter.TYPE_CORE
        private var trimmedFields: HashMap<TrimmedField, Int>? = null

        fun asCrash(isCrash: Boolean) = apply {
            this.isCrashReport = isCrash
        }

        fun withTrimmedFields(trimmedFields: HashMap<TrimmedField, Int>) = apply {
            this.trimmedFields = trimmedFields
        }

        fun withMetricaServiceDataReporterType(type: Int) = apply {
            this.metricaServiceDataReporterType = type
        }

        fun build() = ReportToSend(
            report,
            isCrashReport,
            metricaServiceDataReporterType,
            trimmedFields,
            ReporterEnvironment(
                ProcessConfiguration(environment.processConfiguration),
                CounterConfiguration(environment.reporterConfiguration),
                environment.initialUserProfileID
            )
        )
    }
}

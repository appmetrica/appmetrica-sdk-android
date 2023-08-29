package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.ClientCounterReport.TrimmedField
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.service.MetricaServiceDataReporter
import io.appmetrica.analytics.internal.CounterConfiguration

class ReportToSend(
    val report: CounterReport,
    val isCrashReport: Boolean,
    val serviceDataReporterType: Int,
    val trimmedFields: HashMap<TrimmedField, Int>?,
    val environment: ReporterEnvironment
) {

    override fun toString(): String {
        return "ReportToSend(" +
            "report=$report, " +
            "serviceDataReporterType=$serviceDataReporterType, " +
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
        private var serviceDataReporterType = MetricaServiceDataReporter.TYPE_CORE
        private var trimmedFields: HashMap<TrimmedField, Int>? = null

        fun asCrash(isCrash: Boolean) = apply {
            this.isCrashReport = isCrash
        }

        fun withTrimmedFields(trimmedFields: HashMap<TrimmedField, Int>) = apply {
            this.trimmedFields = trimmedFields
        }

        fun withServiceDataReporterType(type: Int) = apply {
            this.serviceDataReporterType = type
        }

        fun build() = ReportToSend(
            report,
            isCrashReport,
            serviceDataReporterType,
            trimmedFields,
            ReporterEnvironment(
                ProcessConfiguration(environment.processConfiguration),
                CounterConfiguration(environment.reporterConfiguration),
                environment.initialUserProfileID
            )
        )
    }
}

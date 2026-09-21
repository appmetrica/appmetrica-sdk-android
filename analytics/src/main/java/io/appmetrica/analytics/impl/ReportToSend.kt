package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.service.AppMetricaServiceDataReporter
import io.appmetrica.analytics.internal.CounterConfiguration

internal class ReportToSend(
    val report: TrimmedCoreClientEvent,
    val isCrashReport: Boolean,
    val serviceDataReporterType: Int,
    val environment: ReporterEnvironment
) {

    override fun toString(): String {
        return "ReportToSend(" +
            "report=$report, " +
            "serviceDataReporterType=$serviceDataReporterType, " +
            "environment=$environment, " +
            "isCrashReport=$isCrashReport" +
            ")"
    }

    companion object {

        @JvmStatic
        fun newBuilder(report: CoreClientEvent, environment: ReporterEnvironment) =
            Builder(report, environment)
    }

    internal class Builder(
        private val report: CoreClientEvent,
        private val environment: ReporterEnvironment
    ) {

        private var isCrashReport = false
        private var serviceDataReporterType = AppMetricaServiceDataReporter.TYPE_CORE

        fun asCrash(isCrash: Boolean) = apply {
            this.isCrashReport = isCrash
        }

        fun withServiceDataReporterType(type: Int) = apply {
            this.serviceDataReporterType = type
        }

        fun build() = ReportToSend(
            TrimmedCoreClientEvent(
                LoggerStorage.getOrCreatePublicLogger(environment.reporterConfiguration.apiKey),
                report
            ),
            isCrashReport,
            serviceDataReporterType,
            ReporterEnvironment(
                ProcessConfiguration(environment.processConfiguration),
                CounterConfiguration(environment.reporterConfiguration),
                environment.mErrorEnvironment,
                environment.initialUserProfileID
            )
        )
    }
}

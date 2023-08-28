package io.appmetrica.analytics.impl.service.commands

import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.impl.AppMetricaConnector
import io.appmetrica.analytics.impl.ReportToSend
import io.appmetrica.analytics.impl.ShouldDisconnectFromServiceChecker
import io.appmetrica.analytics.internal.IAppMetricaService

internal open class ReportCallable(
    serviceConnector: AppMetricaConnector,
    shouldDisconnectFromServiceChecker: ShouldDisconnectFromServiceChecker?,
    private val reportToSend: ReportToSend
) : ReportToServiceCallable(
    serviceConnector,
    shouldDisconnectFromServiceChecker
) {

    private val tag = "[ReportCallable]"

    override fun reportToService(service: IAppMetricaService) {
        val reportData = reportToSend.report
        val reporterEnvironment = reportToSend.environment
        YLogger.debug(
            tag,
            "send event $reportData " +
                "with environment ${reporterEnvironment.processConfiguration.customHosts} " +
                "with metricaServiceDataReporterType ${reportToSend.metricaServiceDataReporterType}"
        )
        service.reportData(
            reportToSend.metricaServiceDataReporterType,
            reportData.toBundle(reporterEnvironment.configBundle)
        )
    }

    override fun onReportToServiceError(throwable: Throwable?) {
        YLogger.error(tag, throwable, "Exception during report sending. Report: $reportToSend")
    }
}

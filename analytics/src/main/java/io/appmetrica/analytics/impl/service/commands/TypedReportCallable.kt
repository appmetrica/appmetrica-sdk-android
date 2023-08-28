package io.appmetrica.analytics.impl.service.commands

import android.os.Bundle
import io.appmetrica.analytics.impl.AppMetricaConnector
import io.appmetrica.analytics.impl.ShouldDisconnectFromServiceChecker
import io.appmetrica.analytics.internal.IAppMetricaService

internal class TypedReportCallable(
    serviceConnector: AppMetricaConnector,
    shouldDisconnectFromServiceChecker: ShouldDisconnectFromServiceChecker?,
    private val type: Int,
    private val bundle: Bundle
) : ReportToServiceCallable(
    serviceConnector,
    shouldDisconnectFromServiceChecker
) {

    override fun reportToService(service: IAppMetricaService) {
        service.reportData(type, bundle)
    }
}

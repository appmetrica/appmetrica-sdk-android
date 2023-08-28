package io.appmetrica.analytics.impl.service.commands

import android.os.Bundle
import io.appmetrica.analytics.impl.AppMetricaConnector
import io.appmetrica.analytics.impl.ShouldDisconnectFromServiceChecker
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.internal.IAppMetricaService

internal class PauseUserSessionCallable(
    serviceConnector: AppMetricaConnector,
    shouldDisconnectFromServiceChecker: ShouldDisconnectFromServiceChecker?,
    private val processConfiguration: ProcessConfiguration
) : ReportToServiceCallable(
    serviceConnector,
    shouldDisconnectFromServiceChecker
) {

    override fun reportToService(service: IAppMetricaService) {
        val bundle = Bundle()
        processConfiguration.toBundle(bundle)
        service.pauseUserSession(bundle)
    }
}

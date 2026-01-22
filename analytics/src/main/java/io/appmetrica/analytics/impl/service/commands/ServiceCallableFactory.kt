package io.appmetrica.analytics.impl.service.commands

import android.content.Context
import android.os.Bundle
import io.appmetrica.analytics.impl.AppMetricaConnector
import io.appmetrica.analytics.impl.ReportToSend
import io.appmetrica.analytics.impl.ShouldDisconnectFromServiceChecker
import io.appmetrica.analytics.impl.client.ProcessConfiguration

internal class ServiceCallableFactory(
    private val context: Context,
    private val serviceConnector: AppMetricaConnector
) {

    var shouldDisconnectFromServiceChecker: ShouldDisconnectFromServiceChecker? = null

    fun createResumeUseSessionCallable(
        processConfiguration: ProcessConfiguration
    ): ReportToServiceCallable {
        return ResumeUserSessionCallable(serviceConnector, shouldDisconnectFromServiceChecker, processConfiguration)
    }

    fun createPauseUseSessionCallable(
        processConfiguration: ProcessConfiguration
    ): ReportToServiceCallable {
        return PauseUserSessionCallable(serviceConnector, shouldDisconnectFromServiceChecker, processConfiguration)
    }

    fun createTypedReportCallable(
        type: Int,
        bundle: Bundle
    ): ReportToServiceCallable {
        return TypedReportCallable(serviceConnector, shouldDisconnectFromServiceChecker, type, bundle)
    }

    fun createReportCallable(
        reportToSend: ReportToSend
    ): ReportToServiceCallable {
        return ReportCallable(serviceConnector, shouldDisconnectFromServiceChecker, reportToSend)
    }

    fun createCrashCallable(
        reportToSend: ReportToSend
    ): ReportToServiceCallable {
        return CrashCallable(context, serviceConnector, shouldDisconnectFromServiceChecker, reportToSend)
    }
}

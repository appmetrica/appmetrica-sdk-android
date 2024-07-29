package io.appmetrica.analytics.impl.reporter

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.AppStatusMonitor
import io.appmetrica.analytics.impl.MainReporterComponents
import io.appmetrica.analytics.impl.ReportsHandler
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger

internal class MainReporterContext(
    mainReporterComponents: MainReporterComponents,
    val config: AppMetricaConfig,
    val publicLogger: PublicLogger,
) {
    val applicationContext: Context = mainReporterComponents.context
    val appStatusMonitor: AppStatusMonitor = mainReporterComponents.appStatusMonitor
    val deviceId: String? = mainReporterComponents.startupHelper.deviceId
    val reportsHandler: ReportsHandler = mainReporterComponents.reportsHandler
}

package io.appmetrica.analytics.impl.reporter

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.AppStatusMonitor
import io.appmetrica.analytics.impl.ReportsHandler
import io.appmetrica.analytics.impl.utils.PublicLogger

internal class MainReporterContext(
    val applicationContext: Context,
    val appStatusMonitor: AppStatusMonitor,
    val config: AppMetricaConfig,
    val deviceId: String?,
    val publicLogger: PublicLogger,
    val reportsHandler: ReportsHandler
)

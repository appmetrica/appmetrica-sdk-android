package io.appmetrica.analytics.impl.reporter

import android.content.Context
import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.impl.ReportsHandler

internal class ManualReporterContext(
    val context: Context,
    val config: ReporterConfig,
    val reportsHandler: ReportsHandler
)

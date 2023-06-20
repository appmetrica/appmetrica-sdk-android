package io.appmetrica.analytics.impl.reporter

import io.appmetrica.analytics.ReporterConfig
import io.appmetrica.analytics.impl.ReportsHandler

internal class ManualReporterContext(
    val config: ReporterConfig,
    val reportsHandler: ReportsHandler
)

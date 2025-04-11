package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.reporter.CrashReporterContext
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.internal.CounterConfigurationReporterType

internal class CrashReporterFieldsProvider(
    processConfiguration: ProcessConfiguration,
    errorEnvironment: ErrorEnvironment,
    val reportsHandler: ReportsHandler,
    config: AppMetricaConfig,
) {

    val reporterEnvironment = ReporterEnvironment(
        processConfiguration,
        CounterConfiguration(config, CounterConfigurationReporterType.CRASH),
        errorEnvironment,
        config.userProfileID
    )

    val crashReporterContext: CrashReporterContext
        get() = CrashReporterContext()
}

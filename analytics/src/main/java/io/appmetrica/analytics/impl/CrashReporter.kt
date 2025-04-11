package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class CrashReporter(
    private val crashReporterFieldsProvider: CrashReporterFieldsProvider
) : IUnhandledSituationReporter {

    private val tag = "[CrashReporter]"

    private val reporterEnvironment: ReporterEnvironment = crashReporterFieldsProvider.reporterEnvironment

    init {
        ClientServiceLocator.getInstance().reporterLifecycleListener?.onCreateCrashReporter(
            crashReporterFieldsProvider.crashReporterContext
        )
    }

    override fun reportUnhandledException(unhandledException: UnhandledException) {
        DebugLogger.info(tag, "reportUnhandledException: %s", unhandledException.exception?.exceptionClass)
        crashReporterFieldsProvider.reportsHandler.reportCrash(unhandledException, reporterEnvironment)
        LoggerStorage.getMainPublicOrAnonymousLogger().info("Unhandled exception received: $unhandledException")
    }

    fun updateConfig(config: AppMetricaConfig) {
        config.errorEnvironment?.forEach {
            reporterEnvironment.putErrorEnvironmentValue(it.key, it.value)
        }
    }
}

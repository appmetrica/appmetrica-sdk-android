package io.appmetrica.analytics.impl.crash.jvm.client

import io.appmetrica.analytics.AnrListener
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class MainReporterAnrController(
    anrReporter: AnrReporter
) {

    private val tag = "[MainReporterAnrController]"

    private var anrMonitoringTimeout: Int = DefaultValues.DEFAULT_ANR_TICKS_COUNT

    private val listener = LibraryAnrListener(anrReporter)
    private val anrMonitor = ANRMonitor(listener)

    @Synchronized
    fun updateConfig(config: AppMetricaConfig) {
        anrMonitoringTimeout = config.anrMonitoringTimeout ?: DefaultValues.DEFAULT_ANR_TICKS_COUNT
        val enabled = config.anrMonitoring ?: DefaultValues.DEFAULT_ANR_COLLECTING_ENABLED
        DebugLogger.info(tag, "updateConfig: anrMonitoringTimeout = $anrMonitoringTimeout; enabled = $enabled")
        if (enabled) {
            anrMonitor.startMonitoring(anrMonitoringTimeout)
        } else {
            anrMonitor.stopMonitoring()
        }
    }

    @Synchronized
    fun enableAnrMonitoring() {
        DebugLogger.info(tag, "enableAnrMonitoring")
        anrMonitor.startMonitoring(anrMonitoringTimeout)
    }

    fun registerListener(listener: AnrListener) {
        DebugLogger.info(tag, "registerListener: $listener")
        anrMonitor.subscribe { listener.onAppNotResponding() }
    }
}

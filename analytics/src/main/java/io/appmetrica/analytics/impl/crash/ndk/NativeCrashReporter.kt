package io.appmetrica.analytics.impl.crash.ndk

import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.logger.internal.YLogger
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrash
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashHandler

class NativeCrashReporter(
    private val reportConsumer: ReportConsumer,
    private val markCrashCompleted: (String) -> Unit,
) : NativeCrashHandler {
    private val tag = "[NativeCrashReporter]"

    override fun newCrash(nativeCrash: NativeCrash) {
        YLogger.debug(tag, "Receive new native crash ${nativeCrash.uuid}")
        val appMetricaNativeCrash = AppMetricaNativeCrash.from(nativeCrash)
        if (appMetricaNativeCrash != null) {
            YLogger.debug(tag, "Report native crash: $appMetricaNativeCrash")
            reportConsumer.consumeCurrentSessionNativeCrash(appMetricaNativeCrash) {
                markCrashCompleted(nativeCrash.uuid)
            }
        } else {
            YLogger.error(tag, "Failed to parse native crash ${nativeCrash.uuid}.")
            markCrashCompleted(nativeCrash.uuid)
        }
    }

    fun reportCrashesFromPrevSession(crashes: List<NativeCrash>) {
        YLogger.debug(tag, "Report ${crashes.size} native crashes from prev session")
        for (crash in crashes) {
            val appMetricaNativeCrash = AppMetricaNativeCrash.from(crash)
            if (appMetricaNativeCrash != null) {
                YLogger.debug(tag, "Report native crash: $appMetricaNativeCrash")
                reportConsumer.consumePrevSessionNativeCrash(appMetricaNativeCrash) {
                    markCrashCompleted(crash.uuid)
                }
            } else {
                YLogger.error(tag, "Failed to parse native crash ${crash.uuid}.")
                markCrashCompleted(crash.uuid)
            }
        }
    }
}

package io.appmetrica.analytics.impl.crash.ndk.service

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrash
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrash
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashHandler

class NativeCrashHandlerImpl(
    context: Context,
    reportConsumer: ReportConsumer,
    private val markCrashCompleted: (String) -> Unit,
    nativeShouldSendCrashPredicateProvider: NativeShouldSendCrashPredicateProvider,
    eventType: InternalEvents,
    subtag: String,
) : NativeCrashHandler {

    private val tag = "[NativeCrashHandler-$subtag]"

    private val executor: ICommonExecutor =
        GlobalServiceLocator.getInstance().serviceExecutorProvider.reportRunnableExecutor

    private val nativeCrashRunnableProvider = NativeCrashRunnableProvider(
        context,
        reportConsumer,
        nativeShouldSendCrashPredicateProvider,
        eventType
    )

    override fun newCrash(nativeCrash: NativeCrash) {
        DebugLogger.info(tag, "new crash with uuid = ${nativeCrash.uuid} and path = ${nativeCrash.dumpFile}")
        val appMetricaNativeCrash = AppMetricaNativeCrash.from(nativeCrash)
        if (appMetricaNativeCrash != null) {
            LoggerStorage.getOrCreatePublicLogger(appMetricaNativeCrash.metadata.apiKey).info(
                "Detected native crash with uuid = ${appMetricaNativeCrash.uuid}"
            )
            DebugLogger.info(tag, "Report native crash: $appMetricaNativeCrash")
            executor.execute(
                nativeCrashRunnableProvider.get(appMetricaNativeCrash) { markCrashCompleted(nativeCrash.uuid) }
            )
        } else {
            DebugLogger.error(tag, "Failed to parse native crash ${nativeCrash.uuid}.")
            markCrashCompleted(nativeCrash.uuid)
        }
    }
}

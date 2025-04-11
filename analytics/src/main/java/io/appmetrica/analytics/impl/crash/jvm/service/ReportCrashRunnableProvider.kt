package io.appmetrica.analytics.impl.crash.jvm.service

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.clients.ClientDescription
import io.appmetrica.analytics.impl.crash.ReadAndReportRunnable
import io.appmetrica.analytics.impl.crash.jvm.JvmCrash
import io.appmetrica.analytics.impl.crash.jvm.JvmCrashReader
import io.appmetrica.analytics.impl.crash.service.ShouldSendCrashNowPredicate
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.utils.concurrency.FileLocksHolder
import java.io.File

internal class ReportCrashRunnableProvider(
    private val context: Context,
    crashEventConsumer: CrashEventConsumer,
    eventType: InternalEvents,
    private val crashPredicate: ShouldSendCrashNowPredicate<JvmCrash>
) {

    private val jvmCrashReader = JvmCrashReader()

    val arguments = CommonArguments(
        StartupRequestConfig.Arguments(),
        CommonArguments.ReporterArguments(),
        null
    )

    private val crashConsumer = Consumer<JvmCrash> {
        val clientDescription = ClientDescription(it.apiKey, it.packageName, it.pid, it.psid, it.reporterType)
        val event = EventsManager.unhandledExceptionFromFileReportEntry(
            eventType,
            it.name,
            it.crashValue,
            it.bytesTruncated,
            it.trimmedFields,
            it.environment,
            LoggerStorage.getOrCreatePublicLogger(it.apiKey)
        )
        crashEventConsumer.consumeCrash(clientDescription, event, arguments)
    }

    fun get(crashFile: File): ReadAndReportRunnable<JvmCrash> {
        return ReadAndReportRunnable(
            crashFile,
            jvmCrashReader,
            jvmCrashReader,
            crashConsumer,
            FileLocksHolder.getInstance(context),
            crashPredicate
        )
    }
}

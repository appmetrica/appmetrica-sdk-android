package io.appmetrica.analytics.impl.crash.jvm.service

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.crash.jvm.JvmCrash
import io.appmetrica.analytics.impl.crash.service.ShouldSendCrashNowPredicate
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.io.File
import java.util.concurrent.Executor

internal class CrashFromFileConsumer(
    context: Context,
    crashEventConsumer: CrashEventConsumer,
    eventType: InternalEvents,
    crashPredicate: ShouldSendCrashNowPredicate<JvmCrash>,
    private val executor: Executor,
    subtag: String,
) : Consumer<File> {

    private val tag = "[CrashFromFileConsumer-$subtag]"

    private val reportCrashRunnableProvider = ReportCrashRunnableProvider(
        context,
        crashEventConsumer,
        eventType,
        crashPredicate
    )

    override fun consume(input: File?) {
        DebugLogger.info(tag, "handle new crash from file: $input")

        input ?: return

        executor.execute(reportCrashRunnableProvider.get(input))
    }
}

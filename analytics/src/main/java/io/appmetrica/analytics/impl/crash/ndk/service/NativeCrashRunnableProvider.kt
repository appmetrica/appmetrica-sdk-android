package io.appmetrica.analytics.impl.crash.ndk.service

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.impl.FileProvider
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.impl.crash.ReadAndReportRunnable
import io.appmetrica.analytics.impl.crash.jvm.converter.NativeCrashConverter
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrash
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashDumpReader
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashHandlerDescription
import io.appmetrica.analytics.impl.utils.concurrency.FileLocksHolder
import java.io.File

internal class NativeCrashRunnableProvider(
    private val context: Context,
    private val reportsConsumer: ReportConsumer,
    private val shouldSendCrashPredicateProvider: NativeShouldSendCrashPredicateProvider,
    private val eventType: InternalEvents,
) {

    private val fileProvider = FileProvider()
    private val nativeCrashConverter = NativeCrashConverter()

    fun get(nativeCrash: AppMetricaNativeCrash, finalizer: Consumer<File>): ReadAndReportRunnable<String> {

        val handlerDescription = NativeCrashHandlerDescription(nativeCrash.source, nativeCrash.handlerVersion)

        val crashDumpReader = NativeCrashDumpReader(handlerDescription, nativeCrashConverter)
        val reportsCreator = NativeCrashReportCreator(nativeCrash, eventType)
        val crashConsumer = NativeCrashConsumer(reportsConsumer, nativeCrash.metadata, reportsCreator)

        return ReadAndReportRunnable(
            fileProvider.getFileByNonNullPath(nativeCrash.dumpFile),
            crashDumpReader,
            finalizer,
            crashConsumer,
            FileLocksHolder.getInstance(context),
            shouldSendCrashPredicateProvider.predicate(nativeCrash)
        )
    }
}

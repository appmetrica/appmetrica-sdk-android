package io.appmetrica.analytics.impl.crash

import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.backport.Function
import io.appmetrica.analytics.impl.crash.service.ShouldSendCrashNowPredicate
import io.appmetrica.analytics.impl.utils.concurrency.FileLocksHolder
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.io.File

internal class ReadAndReportRunnable<Output>(
    private val crashFile: File,
    private val fileReader: Function<File, Output?>,
    private val finalizator: Consumer<File>,
    private val crashConsumer: Consumer<Output>,
    private val fileLocksHolder: FileLocksHolder,
    private val shouldSendCrashNowPredicate: ShouldSendCrashNowPredicate<Output>
) : Runnable {

    private val tag = "[ReadAndReportRunnable]"

    override fun run() {
        if (crashFile.exists()) {
            val fileLock = fileLocksHolder.getOrCreate(crashFile.name)
            var postProcessAction = finalizator
            try {
                fileLock.lock()
                if (!crashFile.exists()) {
                    return // crash was processed from another thread
                }
                val result = fileReader.apply(crashFile)
                if (result != null) {
                    if (shouldSendCrashNowPredicate.shouldSend(result)) {
                        DebugLogger.info(tag, "for file ${crashFile.name} result is $result")
                        crashConsumer.consume(result)
                    } else {
                        DebugLogger.info(tag, "for file ${crashFile.name} sending forbid by should send predicate")
                        postProcessAction = Consumer {}
                    }
                } else {
                    DebugLogger.info(tag, "for file ${crashFile.name} result is null")
                }
            } catch (exception: Throwable) {
                DebugLogger.error(
                    tag,
                    exception,
                    "can't handle crash in file ${crashFile.name} due to Exception"
                )
            } finally {
                postProcessAction.consume(crashFile)
                fileLock.unlockAndClear()
                fileLocksHolder.clear(crashFile.name)
            }
        }
    }
}

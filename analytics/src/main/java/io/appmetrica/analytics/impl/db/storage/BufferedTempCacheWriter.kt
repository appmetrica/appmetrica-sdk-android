package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.coreutils.internal.buffering.AccumulatingTaskBuffer
import io.appmetrica.analytics.coreutils.internal.buffering.DeferredBatchExecutor
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class BufferedTempCacheWriter(
    private val delayMillis: Long = 1000,
    private val batchWriter: (List<TempCachePutTask>) -> Unit
) {

    private val tag = "[BufferedTempCacheWriter]"

    private val deferredExecutor = DeferredBatchExecutor(
        executor = GlobalServiceLocator.getInstance().serviceExecutorProvider.getPersistenceExecutor(),
        buffer = AccumulatingTaskBuffer(),
        processor = ::processTasks,
        delayMillis = delayMillis,
        tag = tag
    )

    fun put(scope: String, timestamp: Long, data: ByteArray) {
        DebugLogger.info(tag, "put called (scope=$scope, timestamp=$timestamp, dataSize=${data.size})")
        deferredExecutor.submit(TempCachePutTask(scope, timestamp, data))
    }

    fun flush() {
        deferredExecutor.flush()
    }

    fun flushAsync() {
        deferredExecutor.flushAsync()
    }

    private fun processTasks(tasks: List<TempCachePutTask>) {
        try {
            DebugLogger.info(tag, "Writing ${tasks.size} temp cache entries")
            batchWriter(tasks)
        } catch (e: Exception) {
            DebugLogger.error(tag, e, "Error writing ${tasks.size} temp cache entries")
        }
    }
}

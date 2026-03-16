package io.appmetrica.analytics.coreutils.internal.io

import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.buffering.DeferredBatchExecutor
import io.appmetrica.analytics.coreutils.internal.buffering.LastValueTaskBuffer
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.io.File

/**
 * Buffered file writer that optimizes IO operations by delaying writes.
 *
 * When [writeString] is called, the value is stored in memory and a delayed write task is scheduled.
 * If [writeString] is called again before the delay expires, the value is updated in memory
 * but the scheduled task is not cancelled. This guarantees that data will be written to disk
 * no later than [delayMillis] after the first [writeString] call in a batch.
 *
 * @param executor The executor to use for delayed write operations
 * @param delayMillis The delay in milliseconds before writing to the file
 * @param file The target file for read/write operations
 */
class BufferedFileWriter(
    private val executor: IHandlerExecutor,
    private val delayMillis: Long,
    private val file: File
) {

    private val tag = "[BufferedFileWriter-${file.name}]"
    private val lock = Any()

    // Track the cached value for reads (protected by lock)
    private var cachedValue: String? = null

    private val deferredExecutor: DeferredBatchExecutor<String> = DeferredBatchExecutor(
        executor = executor,
        buffer = LastValueTaskBuffer(),
        processor = { tasks ->
            val value = tasks.first()
            try {
                file.parentFile?.takeUnless { it.exists() }?.let { parent ->
                    DebugLogger.info(tag, "Creating parent directory: ${parent.path}")
                    parent.mkdirs()
                }

                file.writeText(value)
                DebugLogger.info(tag, "Successfully wrote to file: ${file.path}, length=${value.length}")

                synchronized(lock) {
                    cachedValue = null
                }
            } catch (e: Exception) {
                DebugLogger.error(tag, e, "Error writing to file: ${file.path}")
            }
        },
        delayMillis = delayMillis,
        tag = tag
    )

    /**
     * Writes a string value to the file with buffering.
     *
     * The value is stored in memory and a delayed write task is scheduled if not already scheduled.
     * If this method is called multiple times within the delay period, the value is updated but
     * the scheduled task is not cancelled, ensuring that data will be written no later than
     * [delayMillis] after the first call. The most recent value will be written to the file.
     *
     * @param value The string value to write
     */
    fun writeString(value: String) {
        synchronized(lock) {
            DebugLogger.info(tag, "writeString called with value length=${value.length}")
            cachedValue = value
        }
        deferredExecutor.submit(value)
    }

    /**
     * Reads the string value from the file or returns the cached value if available.
     *
     * If there's a pending write operation, returns the buffered value.
     * Otherwise, reads the value from the file and caches it to avoid repeated disk reads.
     *
     * @return The string value, or null if the file doesn't exist and there's no cached value
     */
    fun readString(): String? = synchronized(lock) {
        cachedValue?.let {
            DebugLogger.info(tag, "Returning cached value, length=${it.length}")
            return@synchronized it
        }

        val content = try {
            if (file.exists()) {
                val fileContent = file.readText()
                DebugLogger.info(tag, "Read from file: ${file.path}, length=${fileContent.length}")
                fileContent
            } else {
                DebugLogger.info(tag, "File does not exist: ${file.path}")
                null
            }
        } catch (e: Exception) {
            DebugLogger.error(tag, e, "Error reading from file: ${file.path}")
            null
        }

        if (content != null) {
            cachedValue = content
            DebugLogger.info(tag, "Cached file content in memory")
        }

        return@synchronized content
    }

    /**
     * Forces an immediate write of any pending value to the file.
     *
     * Cancels the scheduled delayed write task and writes immediately.
     * This is useful when you need to ensure data is persisted before shutdown or
     * when entering background.
     */
    fun flush() {
        deferredExecutor.flush()
    }

    /**
     * Schedules an asynchronous flush of any pending value to the file.
     *
     * Unlike [flush], this method does not block and performs the flush
     * on the background thread. There is no guarantee about when the flush
     * will complete.
     */
    fun flushAsync() {
        deferredExecutor.flushAsync()
    }
}

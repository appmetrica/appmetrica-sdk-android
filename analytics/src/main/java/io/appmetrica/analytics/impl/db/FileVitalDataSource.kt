package io.appmetrica.analytics.impl.db

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.io.BufferedFileWriter
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.coreutils.internal.io.FileUtils.copyToNullable
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class FileVitalDataSource(
    private val context: Context,
    private val fileName: String,
    private val executor: IHandlerExecutor,
    private val writeDelayMillis: Long = DEFAULT_WRITE_DELAY_MILLIS
) : VitalDataSource {

    private val tag = "[FileVitalDataSource-$fileName]"
    private val lock = Any()

    @Volatile
    private var bufferedWriter: BufferedFileWriter? = null

    @Volatile
    private var migrationCompleted = false

    companion object {
        private const val DEFAULT_WRITE_DELAY_MILLIS = 1000L
    }

    override fun getVitalData(): String? = try {
        getOrCreateBufferedWriter()?.let { writer ->
            performMigrationIfNeeded()
            DebugLogger.info(tag, "Read data from file with name = $fileName")
            writer.readString()
        }
    } catch (ex: Throwable) {
        DebugLogger.error(tag, ex, "File $fileName exception")
        null
    }

    override fun putVitalData(data: String) {
        DebugLogger.info(tag, "Write data to file with name = $fileName, data length = ${data.length}")
        try {
            getOrCreateBufferedWriter()?.writeString(data)
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex, "File $fileName exception")
        }
    }

    override fun flush() {
        DebugLogger.info(tag, "Flush")
        try {
            getOrCreateBufferedWriter()?.flush()
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex, "Exception during flush")
        }
    }

    override fun flushAsync() {
        DebugLogger.info(tag, "FlushAsync")
        try {
            getOrCreateBufferedWriter()?.flushAsync()
        } catch (ex: Throwable) {
            DebugLogger.error(tag, ex, "Exception during flushAsync")
        }
    }

    /**
     * Thread-safe lazy initialization of BufferedFileWriter.
     */
    private fun getOrCreateBufferedWriter(): BufferedFileWriter? {
        bufferedWriter?.let { return it }

        synchronized(lock) {
            bufferedWriter?.let { return it }

            val file = FileUtils.getFileFromSdkStorage(context, fileName) ?: run {
                DebugLogger.info(tag, "Cannot get file from SDK storage for $fileName")
                return null
            }

            return BufferedFileWriter(executor, writeDelayMillis, file).also {
                bufferedWriter = it
            }
        }
    }

    /**
     * Performs one-time migration of data from old location to new location.
     */
    private fun performMigrationIfNeeded() {
        if (migrationCompleted) return

        synchronized(lock) {
            if (migrationCompleted) return

            try {
                val targetFile = FileUtils.getFileFromSdkStorage(context, fileName)
                if (targetFile != null && !targetFile.exists()) {
                    DebugLogger.info(
                        tag,
                        "Vital data for path = `${targetFile.path}` does not exist. Try to import from old location"
                    )
                    FileUtils.getFileFromAppStorage(context, fileName)?.copyToNullable(targetFile)
                }
            } catch (ex: Throwable) {
                DebugLogger.error(tag, ex, "Migration failed for $fileName")
            } finally {
                migrationCompleted = true
            }
        }
    }
}

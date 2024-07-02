package io.appmetrica.analytics.ndkcrashes.impl.utils

import android.content.Context
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.util.concurrent.Semaphore

class SuspendableFileLocker private constructor(context: Context, simpleFileName: String) {
    private class Lock(private val lockFile: File) {
        private val tag = "[SuspendableFileLocker]"

        private val stream: RandomAccessFile = RandomAccessFile(lockFile, "rw")
        private val channel: FileChannel = stream.channel
        private val lock: FileLock = channel.lock()

        fun release() {
            releaseFileLock(lockFile.name, lock)
            channel.safelyClose()
            stream.safelyClose()
        }

        private fun releaseFileLock(fileName: String?, lock: FileLock?) {
            if (lock != null && lock.isValid) {
                try {
                    lock.release()
                    DebugLogger.info(tag, "Lock released for $fileName.")
                } catch (e: IOException) {
                    DebugLogger.error(tag, "Failed to release lock for $fileName.", e)
                }
            }
        }

        private fun Closeable.safelyClose() = try {
            close()
        } catch (exception: Throwable) {
            // Do nothing
        }
    }

    private val semaphore = Semaphore(1, true)
    private val mLockFileName: String = "$simpleFileName.lock"
    private val lockFile: File? = context.cacheDir?.let { File(it, "appmetrica_locks") }?.let { lockDir ->
        lockDir.mkdirs()
        File(lockDir, mLockFileName)
    }

    private var lock: Lock? = null

    @Synchronized
    @Throws(Throwable::class)
    fun lock() {
        semaphore.acquire()
        checkNotNull(lockFile) { "Lock file is null" }
        lock = lock ?: Lock(lockFile)
    }

    @Synchronized
    fun unlock() {
        semaphore.release()
        if (semaphore.availablePermits() > 0) {
            lock?.release()
            lock = null
        }
    }

    companion object {
        private val sLocks = HashMap<String, SuspendableFileLocker>()

        @Synchronized
        fun getLock(context: Context, simpleFileName: String): SuspendableFileLocker {
            return sLocks.getOrPut(simpleFileName) { SuspendableFileLocker(context, simpleFileName) }
        }
    }
}

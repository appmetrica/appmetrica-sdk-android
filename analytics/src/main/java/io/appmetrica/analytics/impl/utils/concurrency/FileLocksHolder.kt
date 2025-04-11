package io.appmetrica.analytics.impl.utils.concurrency

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.VisibleForTesting

class FileLocksHolder @VisibleForTesting internal constructor(private val context: Context) {
    private val locks: MutableMap<String, ExclusiveMultiProcessFileLock> = HashMap()

    @Synchronized
    fun getOrCreate(fileName: String): ExclusiveMultiProcessFileLock {
        return locks.getOrPut(fileName) { ExclusiveMultiProcessFileLock(context, fileName) }
    }

    @Synchronized
    fun clear(fileName: String) {
        locks.remove(fileName)
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var INSTANCE: FileLocksHolder

        @JvmStatic
        fun getInstance(context: Context): FileLocksHolder {
            if (!::INSTANCE.isInitialized) {
                synchronized(FileLocksHolder::class) {
                    if (!::INSTANCE.isInitialized) {
                        INSTANCE = FileLocksHolder(context)
                    }
                }
            }
            return INSTANCE
        }

        @VisibleForTesting
        fun stubInstance(value: FileLocksHolder) {
            INSTANCE = value
        }
    }
}

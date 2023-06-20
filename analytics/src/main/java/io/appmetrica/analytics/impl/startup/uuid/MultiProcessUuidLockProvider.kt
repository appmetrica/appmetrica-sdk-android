package io.appmetrica.analytics.impl.startup.uuid

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.impl.db.FileConstants
import io.appmetrica.analytics.impl.utils.concurrency.ExclusiveMultiProcessFileLock

object MultiProcessUuidLockProvider {
    private var lock: ExclusiveMultiProcessFileLock? = null

    @Synchronized
    @JvmStatic
    fun getLock(context: Context): ExclusiveMultiProcessFileLock {
        var localCopy = lock
        if (localCopy == null) {
            localCopy = ExclusiveMultiProcessFileLock(context, FileConstants.UUID_FILE_NAME)
            lock = localCopy
        }
        return localCopy
    }

    @VisibleForTesting
    @JvmStatic
    fun reset() {
        lock = null
    }
}

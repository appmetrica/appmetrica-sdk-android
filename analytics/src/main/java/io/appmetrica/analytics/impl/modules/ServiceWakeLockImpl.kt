package io.appmetrica.analytics.impl.modules

import android.content.Context
import android.content.ServiceConnection
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ServiceWakeLock

private const val ACTION_PREFIX = "io.appmetrica.analytics.ACTION_SERVICE_WAKELOCK"

class ServiceWakeLockImpl(
    private val context: Context,
    private val serviceWakeLockBinder: ServiceWakeLockBinder
) : ServiceWakeLock {
    private val tag = "[ServiceWakeLockImpl]"

    private val wakeLocks = HashMap<String, ServiceConnection?>()

    @Synchronized
    override fun acquireWakeLock(wakeLockId: String): Boolean {
        DebugLogger.info(tag, "Attempt to acquire new wakeLock with id = $wakeLockId")
        if (wakeLocks[wakeLockId] == null) {
            DebugLogger.info(tag, "Acquire new wakeLock with id = $wakeLockId")
            wakeLocks[wakeLockId] = serviceWakeLockBinder.bindService(context, action(wakeLockId))
        }
        return wakeLocks[wakeLockId] != null
    }

    @Synchronized
    override fun releaseWakeLock(wakeLockId: String) {
        DebugLogger.info(tag, "Attempt to release wakeLock with id = $wakeLockId")
        wakeLocks[wakeLockId]?.let {
            serviceWakeLockBinder.unbindService(action(wakeLockId), context, it)
            wakeLocks.remove(wakeLockId)
            DebugLogger.info(tag, "Release wakelock with id = $wakeLockId")
        }
    }

    fun action(wakeLockId: String) = "$ACTION_PREFIX.$wakeLockId"
}

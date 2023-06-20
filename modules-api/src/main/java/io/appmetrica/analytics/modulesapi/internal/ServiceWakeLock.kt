package io.appmetrica.analytics.modulesapi.internal

interface ServiceWakeLock {

    fun acquireWakeLock(wakeLockId: String): Boolean

    fun releaseWakeLock(wakeLockId: String)
}

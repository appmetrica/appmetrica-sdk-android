package io.appmetrica.analytics.modulesapi.internal.service

interface ServiceWakeLock {

    fun acquireWakeLock(wakeLockId: String): Boolean

    fun releaseWakeLock(wakeLockId: String)
}

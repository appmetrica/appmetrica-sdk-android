package io.appmetrica.analytics.ndkcrashes.impl

import android.util.Log
import io.appmetrica.analytics.ndkcrashes.BuildConfig

object NativeCrashLogger {
    private const val enabled: Boolean = BuildConfig.APPMETRICA_DEBUG

    fun debug(tag: String, message: String) {
        if (enabled) {
            Log.d(tag, message)
        }
    }

    fun warning(tag: String, message: String, error: Throwable? = null) {
        if (enabled) {
            Log.w(tag, message, error)
        }
    }

    fun error(tag: String, message: String, error: Throwable? = null) {
        if (enabled) {
            Log.e(tag, message, error)
        }
    }
}

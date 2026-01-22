package io.appmetrica.analytics.ndkcrashes.impl.utils

import android.os.Build

internal object AndroidUtils {
    fun isAndroidMAchieved(): Boolean = isApiAchieved(Build.VERSION_CODES.M)
    fun isAndroidNAchieved(): Boolean = isApiAchieved(Build.VERSION_CODES.N)
    fun isAndroidQAchieved(): Boolean = isApiAchieved(Build.VERSION_CODES.Q)

    private fun isApiAchieved(levelOfApi: Int): Boolean {
        return Build.VERSION.SDK_INT >= levelOfApi
    }
}

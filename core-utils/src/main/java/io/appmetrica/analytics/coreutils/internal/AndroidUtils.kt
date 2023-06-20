package io.appmetrica.analytics.coreutils.internal

import android.os.Build

object AndroidUtils {

    @JvmStatic
    fun isApiAchieved(levelOfApi: Int): Boolean {
        return Build.VERSION.SDK_INT >= levelOfApi
    }
}

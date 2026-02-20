package io.appmetrica.analytics.coreutils.internal

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

object AndroidUtils {

    @ChecksSdkIntAtLeast(parameter = 0)
    @JvmStatic
    fun isApiAchieved(levelOfApi: Int): Boolean {
        return Build.VERSION.SDK_INT >= levelOfApi
    }
}

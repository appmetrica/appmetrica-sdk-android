package io.appmetrica.analytics.coreutils.internal

import android.annotation.SuppressLint
import android.os.Build

object AndroidUtils {

    @SuppressLint("AnnotateVersionCheck")
    @JvmStatic
    fun isApiAchieved(levelOfApi: Int): Boolean {
        return Build.VERSION.SDK_INT >= levelOfApi
    }
}

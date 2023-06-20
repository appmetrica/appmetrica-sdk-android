package io.appmetrica.analytics.coreutils.internal.network

import android.os.Build
import java.util.Locale

object UserAgent {

    @JvmStatic
    fun getFor(
        sdkName: String,
        versionName: String,
        buildNumber: String
    ): String =
        "$sdkName/$versionName.$buildNumber (${formDeviceName()}; Android ${Build.VERSION.RELEASE})"

    private fun formDeviceName(): String {
        return if (Build.MODEL.startsWith(Build.MANUFACTURER)) {
            Build.MODEL
        } else {
            Build.MANUFACTURER + " " + Build.MODEL
        }.capitalize(Locale.US)
    }
}

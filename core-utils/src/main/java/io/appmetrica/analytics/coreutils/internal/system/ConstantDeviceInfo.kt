package io.appmetrica.analytics.coreutils.internal.system

import android.annotation.SuppressLint
import android.os.Build

object ConstantDeviceInfo {

    const val APP_PLATFORM: String = "android"

    @JvmField
    val MANUFACTURER: String = Build.MANUFACTURER

    @JvmField
    val MODEL: String = Build.MODEL

    @JvmField
    val OS_VERSION: String = Build.VERSION.RELEASE

    @SuppressLint("AnnotateVersionCheck")
    @JvmField
    val OS_API_LEVEL: Int = Build.VERSION.SDK_INT

    @JvmField
    val DEVICE_ROOT_STATUS: String = RootChecker.isRootedPhone().toString()
}

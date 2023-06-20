package io.appmetrica.analytics.coreutils.internal.services

import android.content.Context
import android.content.pm.PackageInfo

class PackageManagerUtils {

    companion object {

        @JvmStatic
        private val safePackageManager = SafePackageManager()

        @JvmStatic
        fun getAppVersionCodeString(context: Context): String = getAppVersionCodeInt(context).toString()

        @JvmStatic
        fun getAppVersionCodeInt(context: Context): Int = getPackageInfo(context)?.versionCode ?: 0

        /**
         * Returns the version of application from an context.
         * @param context `Context` object.
         * @return Version.
         */
        @JvmStatic
        fun getAppVersionName(context: Context): String = getPackageInfo(context)?.versionName ?: "0.0"

        @JvmStatic
        fun getPackageInfo(context: Context): PackageInfo? =
            safePackageManager.getPackageInfo(context, context.packageName)
    }
}

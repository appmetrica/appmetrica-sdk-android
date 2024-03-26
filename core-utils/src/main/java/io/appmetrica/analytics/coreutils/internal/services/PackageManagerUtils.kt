package io.appmetrica.analytics.coreutils.internal.services

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.ProviderInfo

object PackageManagerUtils {

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

    @JvmStatic
    fun hasContentProvider(context: Context, authority: String): Boolean =
        resolveContentProvider(context, authority) != null

    @JvmStatic
    fun resolveContentProvider(context: Context, authority: String): ProviderInfo? =
        safePackageManager.resolveContentProvider(context, authority)
}

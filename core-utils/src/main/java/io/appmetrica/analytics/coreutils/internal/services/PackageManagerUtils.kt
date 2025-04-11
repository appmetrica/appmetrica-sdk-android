package io.appmetrica.analytics.coreutils.internal.services

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo

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
    fun getServiceInfo(context: Context, clazz: Class<*>): ServiceInfo? {
        return safePackageManager.getServiceInfo(
            context,
            ComponentName(context, clazz),
            PackageManager.GET_SERVICES
        )
    }

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

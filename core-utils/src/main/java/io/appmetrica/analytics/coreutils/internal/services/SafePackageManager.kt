package io.appmetrica.analytics.coreutils.internal.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.os.Build
import io.appmetrica.analytics.coreutils.internal.AndroidUtils.isApiAchieved
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManagerHelperForR.extractPackageInstaller
import io.appmetrica.analytics.logger.internal.YLogger

class SafePackageManager {

    private val tag = "[SafePackageManager]"

    fun getPackageInfo(context: Context, packageName: String): PackageInfo? = getPackageInfo(context, packageName, 0)

    fun getPackageInfo(context: Context, packageName: String, flags: Int): PackageInfo? = try {
        context.packageManager.getPackageInfo(packageName, flags)
    } catch (e: Throwable) {
        YLogger.error(tag, e, e.message)
        null
    }

    fun getServiceInfo(context: Context, component: ComponentName, flags: Int): ServiceInfo? = try {
        context.packageManager.getServiceInfo(component, flags)
    } catch (e: Throwable) {
        YLogger.error(tag, e, e.message)
        null
    }

    fun resolveService(context: Context, intent: Intent, flags: Int): ResolveInfo? = try {
        context.packageManager.resolveService(intent, flags)
    } catch (e: Throwable) {
        YLogger.error(tag, e, e.message)
        null
    }

    fun resolveActivity(context: Context, intent: Intent, flags: Int): ResolveInfo? = try {
        context.packageManager.resolveActivity(intent, flags)
    } catch (e: Throwable) {
        YLogger.error(tag, e, e.message)
        null
    }

    fun resolveContentProvider(context: Context, authority: String): ProviderInfo? = try {
        val pm = context.packageManager
        if (isApiAchieved(Build.VERSION_CODES.TIRAMISU)) {
            PackageManagerUtilsTiramisu.resolveContentProvider(pm, authority)
        } else {
            pm.resolveContentProvider(authority, PackageManager.GET_META_DATA)
        }
    } catch (e: Throwable) {
        YLogger.error(tag, e, e.message)
        null
    }

    fun getApplicationInfo(context: Context, packageName: String, flags: Int): ApplicationInfo? = try {
        context.packageManager.getApplicationInfo(packageName, flags)
    } catch (e: Throwable) {
        YLogger.error(tag, e, e.message)
        null
    }

    fun getActivityInfo(context: Context, componentName: ComponentName, flags: Int): ActivityInfo? = try {
        context.packageManager.getActivityInfo(componentName, flags)
    } catch (e: Throwable) {
        YLogger.error(tag, e, e.message)
        null
    }

    fun setComponentEnabledSetting(context: Context, componentName: ComponentName, newState: Int, flags: Int) {
        try {
            val pm = context.packageManager
            pm.setComponentEnabledSetting(componentName, newState, flags)
        } catch (e: Throwable) {
            YLogger.error(tag, e, e.message)
        }
    }

    fun hasSystemFeature(context: Context, name: String): Boolean = try {
        context.packageManager.hasSystemFeature(name)
    } catch (e: Throwable) {
        YLogger.error(tag, e, e.message)
        false
    }

    fun getInstallerPackageName(context: Context, packageName: String): String? = try {
        val pm = context.packageManager
        val installer = if (isApiAchieved(Build.VERSION_CODES.R)) {
            extractPackageInstaller(pm, packageName)
        } else {
            pm.getInstallerPackageName(packageName)
        }
        YLogger.info(tag, "AppInstaller = %s", installer)
        installer
    } catch (e: Throwable) {
        YLogger.error(tag, e, e.message)
        null
    }
}

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
import io.appmetrica.analytics.logger.internal.YLogger

class SafePackageManager {

    private val tag = "[SafePackageManager]"

    fun getPackageInfo(context: Context, packageName: String): PackageInfo? = getPackageInfo(context, packageName, 0)

    fun getPackageInfo(context: Context, packageName: String, flags: Int): PackageInfo? = runSafely {
        context.packageManager.getPackageInfo(packageName, flags)
    }

    fun getServiceInfo(context: Context, component: ComponentName, flags: Int): ServiceInfo? = runSafely {
        context.packageManager.getServiceInfo(component, flags)
    }

    fun resolveService(context: Context, intent: Intent, flags: Int): ResolveInfo? = runSafely {
        context.packageManager.resolveService(intent, flags)
    }

    fun resolveActivity(context: Context, intent: Intent, flags: Int): ResolveInfo? = runSafely {
        context.packageManager.resolveActivity(intent, flags)
    }

    fun resolveContentProvider(context: Context, authority: String): ProviderInfo? = runSafely {
        val pm = context.packageManager
        if (isApiAchieved(Build.VERSION_CODES.TIRAMISU)) {
            PackageManagerUtilsTiramisu.resolveContentProvider(pm, authority)
        } else {
            pm.resolveContentProvider(authority, PackageManager.GET_META_DATA)
        }
    }

    fun getApplicationInfo(context: Context, packageName: String, flags: Int): ApplicationInfo? = runSafely {
        context.packageManager.getApplicationInfo(packageName, flags)
    }

    fun getActivityInfo(context: Context, componentName: ComponentName, flags: Int): ActivityInfo? = runSafely {
        context.packageManager.getActivityInfo(componentName, flags)
    }

    fun setComponentEnabledSetting(
        context: Context,
        componentName: ComponentName,
        newState: Int,
        flags: Int
    ) = runSafely { context.packageManager.setComponentEnabledSetting(componentName, newState, flags) }

    fun hasSystemFeature(context: Context, name: String): Boolean = runSafely(defaultValue = false) {
        context.packageManager.hasSystemFeature(name)
    }

    fun getInstallerPackageName(context: Context, packageName: String): String? = runSafely {
        val pm = context.packageManager
        val installer = if (isApiAchieved(Build.VERSION_CODES.R)) {
            SafePackageManagerHelperForR.extractPackageInstaller(pm, packageName)
        } else {
            pm.getInstallerPackageName(packageName)
        }
        YLogger.info(tag, "AppInstaller = %s", installer)
        installer
    }

    private fun <T> runSafely(block: () -> T): T? = runSafely(defaultValue = null, block)

    private fun <T> runSafely(defaultValue: T, block: () -> T?): T = try {
        block() ?: defaultValue
    } catch (e: Throwable) {
        YLogger.error(tag, e, e.message)
        defaultValue
    }
}

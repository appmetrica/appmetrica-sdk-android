package io.appmetrica.analytics.coreutils.internal.services

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import io.appmetrica.analytics.coreapi.internal.annotations.DoNotInline

@DoNotInline
@TargetApi(Build.VERSION_CODES.R)
object SafePackageManagerHelperForR {
    @JvmStatic
    fun extractPackageInstaller(packageManager: PackageManager, packageName: String): String? {
        return packageManager.getInstallSourceInfo(packageName).installingPackageName
    }
}

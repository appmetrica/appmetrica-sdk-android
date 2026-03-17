package io.appmetrica.analytics.impl.referrer.service.provider

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.referrer.service.cache.CachedReferrerProvider
import io.appmetrica.analytics.impl.referrer.service.cache.VitalReferrerCache
import io.appmetrica.analytics.impl.referrer.service.provider.google.GoogleReferrerProvider
import io.appmetrica.analytics.impl.referrer.service.provider.huawei.HuaweiReferrerProvider

internal class ReferrerProviderFactory(
    private val packageManager: SafePackageManager = SafePackageManager(),
) {
    fun create(context: Context): ReferrerProvider {
        val globalServiceLocator = GlobalServiceLocator.getInstance()
        val executor = globalServiceLocator.serviceExecutorProvider.supportIOExecutor
        val vitalCommonDataProvider = globalServiceLocator.vitalDataProviderStorage.commonDataProvider

        val packageInstaller = packageManager.getInstallerPackageName(context, context.packageName)
        val provider = when (packageInstaller) {
            "com.android.vending" -> GoogleReferrerProvider(context, executor)
            "com.huawei.appmarket" -> HuaweiReferrerProvider(context)
            else -> NotSupportedPackageInstallerReferrerProvider(packageInstaller)
        }

        val vitalCachedProvider = CachedReferrerProvider(provider, VitalReferrerCache(vitalCommonDataProvider))
        val safeProvider = SafeReferrerProvider(vitalCachedProvider)
        val executorProvider = ExecutorReferrerProvider(safeProvider, executor)

        return executorProvider
    }
}

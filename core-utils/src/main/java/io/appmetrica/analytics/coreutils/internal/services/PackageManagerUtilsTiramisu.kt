package io.appmetrica.analytics.coreutils.internal.services

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.os.Build
import io.appmetrica.analytics.coreapi.internal.annotations.DoNotInline
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

@DoNotInline
@TargetApi(Build.VERSION_CODES.TIRAMISU)
internal object PackageManagerUtilsTiramisu {

    private const val tag = "[PackageManagerUtilsTiramisu]"

    fun resolveContentProvider(packageManager: PackageManager, uri: String): ProviderInfo? =
        try {
            packageManager.resolveContentProvider(
                uri,
                PackageManager.ComponentInfoFlags.of(PackageManager.GET_PROVIDERS.toLong())
            )
        } catch (e: Throwable) {
            DebugLogger.info(tag, e.message ?: "", e)
            null
        }
}

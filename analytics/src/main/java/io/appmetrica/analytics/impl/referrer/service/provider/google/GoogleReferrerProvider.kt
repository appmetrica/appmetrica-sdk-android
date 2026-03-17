package io.appmetrica.analytics.impl.referrer.service.provider.google

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.impl.referrer.service.provider.ReferrerProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class GoogleReferrerProvider(
    private val context: Context,
    private val executor: ICommonExecutor,
) : ReferrerProvider {
    private val tag = "[GoogleReferrerProvider]"

    override val referrerName: String = "google"

    override fun requestReferrer(listener: ReferrerListener) {
        val hasLibrary = hasInstallReferrerLibrary()
        DebugLogger.info(tag, "Try to get $referrerName referrer. Has Google Play referrer library? $hasLibrary")

        if (hasLibrary) {
            GooglePlayReferrerLibrary(executor).requestReferrer(context, listener)
        } else {
            listener.onResult(ReferrerResult.Failure("Google Play Install Referrer library is not detected"))
        }
    }

    private fun hasInstallReferrerLibrary(): Boolean {
        return ReflectionUtils.detectClassExists("com.android.installreferrer.api.InstallReferrerClient")
    }
}

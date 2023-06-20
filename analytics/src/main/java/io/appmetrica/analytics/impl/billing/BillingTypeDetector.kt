package io.appmetrica.analytics.impl.billing

import io.appmetrica.analytics.billinginterface.internal.BillingType
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils

private const val LOG_TAG = "[BillingTypeDetector]"

internal object BillingTypeDetector {

    @JvmStatic
    fun getBillingType(): BillingType {
        val version = try {
            ReflectionUtils
                .findClass("com.android.billingclient.BuildConfig")
                ?.getField("VERSION_NAME")
                ?.get(null) as String?
        } catch (e: Throwable) {
            YLogger.info(LOG_TAG, "Failed to get billing library version: $e")
            null
        }
        YLogger.info(LOG_TAG, "Billing library version: $version")

        return when {
            version == null -> BillingType.NONE
            version.startsWith("2.") -> BillingType.LIBRARY_V3
            version.startsWith("3.") -> BillingType.LIBRARY_V3
            version.startsWith("4.") -> BillingType.LIBRARY_V4
            else -> BillingType.LIBRARY_V4
        }
    }
}

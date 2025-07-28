package io.appmetrica.analytics.impl.billing

import io.appmetrica.analytics.billinginterface.internal.BillingType
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

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
            DebugLogger.info(LOG_TAG, "Failed to get billing library version: $e")
            null
        }
        DebugLogger.info(LOG_TAG, "Billing library version: $version")

        return when {
            version.isNullOrBlank() -> BillingType.NONE
            version.startsWith("2.") -> BillingType.NONE
            version.startsWith("3.") -> BillingType.NONE
            version.startsWith("4.") -> BillingType.NONE
            version.startsWith("5.") -> BillingType.LIBRARY_V6
            version.startsWith("6.") -> BillingType.LIBRARY_V6
            version.startsWith("7.") -> BillingType.LIBRARY_V6
            version.startsWith("8.") -> BillingType.LIBRARY_V8
            else -> BillingType.LIBRARY_V8
        }
    }
}

package io.appmetrica.analytics.billing.impl

import io.appmetrica.analytics.billinginterface.internal.BillingType
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

private const val LOG_TAG = "[BillingTypeDetector]"
private const val LIBRARY_V8_MIN_MAJOR_VERSION = 8

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

        val majorVersion = version
            ?.takeIf { it.isNotBlank() }
            ?.substringBefore('.')
            ?.toIntOrNull()

        return when {
            majorVersion == null -> BillingType.NONE
            majorVersion < LIBRARY_V8_MIN_MAJOR_VERSION -> BillingType.NONE
            majorVersion == LIBRARY_V8_MIN_MAJOR_VERSION -> BillingType.LIBRARY_V8
            else -> BillingType.LIBRARY_V8
        }
    }
}

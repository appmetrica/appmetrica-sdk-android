package io.appmetrica.analytics.impl.utils

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.IOUtils
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter
import io.appmetrica.analytics.impl.id.AppSetIdGetter
import io.appmetrica.analytics.impl.id.Constants
import io.appmetrica.analytics.impl.id.TimesBasedRetryStrategy
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.Locale
import java.util.UUID

private const val TAG = "[DeviceIdGenerator]"

internal class DeviceIdGenerator @VisibleForTesting constructor(
    private val context: Context,
    private val advertisingIdGetter: AdvertisingIdGetter,
    private val appSetIdGetter: AppSetIdGetter
) {

    constructor(context: Context) : this(
        context,
        GlobalServiceLocator.getInstance().serviceInternalAdvertisingIdGetter,
        GlobalServiceLocator.getInstance().appSetIdGetter
    )

    fun generateDeviceId(): String {
        val yandexAdvId = advertisingIdGetter.getIdentifiersForced(context, TimesBasedRetryStrategy(5, 500)).yandex
        DebugLogger.info(TAG, "Yandex Adv ID: $yandexAdvId")
        return if (yandexAdvId.isValid) {
            // !! is safe because of isValid check
            DebugLogger.info(TAG, "Yandex Adv ID is valid. Using it for device ID")
            StringUtils.toHexString(IOUtils.md5(yandexAdvId.mAdTrackingInfo!!.advId!!.toByteArray()))
        } else {
            val appSetId = appSetIdGetter.getAppSetId().id
            DebugLogger.info(TAG, "Yandex Adv ID is not valid. App Set ID: $appSetId")
            if (appSetId != null && appSetId.isValidAppSetId()) {
                appSetId.replace("-", "")
            } else {
                generateRandomIdentifierWithUuidPattern()
            }
        }
    }

    private fun String.isValidAppSetId(): Boolean {
        return !isNullOrEmpty() && isValidUuid() && this != Constants.INVALID_ADV_ID
    }

    private fun String.isValidUuid(): Boolean = try {
        UUID.fromString(this)
        true
    } catch (ex: Throwable) {
        false
    }

    private fun generateRandomIdentifierWithUuidPattern(): String =
        UUID.randomUUID().toString().replace("-", "").lowercase(Locale.US)
}

package io.appmetrica.analytics.identifiers.impl.huawei

import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.identifiers.impl.AdIdServiceConnectionController
import io.appmetrica.analytics.identifiers.impl.AdsIdInfo
import io.appmetrica.analytics.identifiers.impl.AdsIdResult
import io.appmetrica.analytics.identifiers.impl.AdvIdProvider
import io.appmetrica.analytics.identifiers.impl.ConnectionException
import io.appmetrica.analytics.identifiers.impl.Constants
import io.appmetrica.analytics.identifiers.impl.getProviderUnavailableResult

private const val TAG = "[Huawei OAID] "
private val HMS_ADV_ID_INTENT =
    Intent("com.uodis.opendevice.OPENIDS_SERVICE").setPackage("com.huawei.hwid")

internal class HuaweiAdvIdGetter @VisibleForTesting internal constructor(
    private val connectionController: AdIdServiceConnectionController<OpenDeviceIdentifierService>
) : AdvIdProvider {

    constructor() : this(
        AdIdServiceConnectionController<OpenDeviceIdentifierService>(
            HMS_ADV_ID_INTENT,
            { OpenDeviceIdentifierService.Stub.asInterface(it) },
            "huawei",
        )
    )

    override fun getAdTrackingInfo(context: Context): AdsIdResult {
        YLogger.info(TAG, "getAdTrackingInfo. Connecting to service...")
        return try {
            val service: OpenDeviceIdentifierService = connectionController.connect(context)
            val oaid = service.oaid
            YLogger.debug(TAG, "id fetched successfully: %s", oaid)
            val isDisabled = service.isOaidTrackLimited
            YLogger.debug(TAG, "mLimitedAdvertisingTracking flag fetched successfully: %b", isDisabled)
            AdsIdResult(IdentifierStatus.OK, AdsIdInfo(Constants.Providers.HUAWEI, oaid, isDisabled))
        } catch (connectionException: ConnectionException) {
            val message = connectionException.message ?: "unknown exception during binding huawei services"
            getProviderUnavailableResult(message)
        } catch (e: Throwable) {
            YLogger.error(TAG, e, "can't fetch adv id.")
            getProviderUnavailableResult("exception while fetching hoaid: " + e.message)
        } finally {
            try {
                connectionController.disconnect(context)
            } catch (ex: Throwable) {
                YLogger.error(TAG, ex, "could not unbind from service")
            }
        }
    }
}

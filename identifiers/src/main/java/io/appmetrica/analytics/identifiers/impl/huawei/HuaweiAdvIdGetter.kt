package io.appmetrica.analytics.identifiers.impl.huawei

import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.identifiers.impl.AdvIdInfo
import io.appmetrica.analytics.identifiers.impl.AdvIdProvider
import io.appmetrica.analytics.identifiers.impl.AdvIdResult
import io.appmetrica.analytics.identifiers.impl.AdvIdServiceConnectionController
import io.appmetrica.analytics.identifiers.impl.ConnectionException
import io.appmetrica.analytics.identifiers.impl.Constants
import io.appmetrica.analytics.identifiers.impl.getProviderUnavailableResult
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

private val HMS_ADV_ID_INTENT =
    Intent("com.uodis.opendevice.OPENIDS_SERVICE").setPackage("com.huawei.hwid")

internal class HuaweiAdvIdGetter @VisibleForTesting internal constructor(
    private val connectionController: AdvIdServiceConnectionController<OpenDeviceIdentifierService>
) : AdvIdProvider {

    private val tag = "[Huawei OAID]"

    constructor() : this(
        AdvIdServiceConnectionController<OpenDeviceIdentifierService>(
            HMS_ADV_ID_INTENT,
            { OpenDeviceIdentifierService.Stub.asInterface(it) },
            "huawei",
        )
    )

    override fun getAdTrackingInfo(context: Context): AdvIdResult {
        DebugLogger.info(tag, "getAdTrackingInfo. Connecting to service...")
        return try {
            val service: OpenDeviceIdentifierService = connectionController.connect(context)
            val oaid = service.oaid
            DebugLogger.info(tag, "id fetched successfully: %s", oaid)
            val isDisabled = service.isOaidTrackLimited
            DebugLogger.info(tag, "mLimitedAdvertisingTracking flag fetched successfully: %b", isDisabled)
            AdvIdResult(IdentifierStatus.OK, AdvIdInfo(Constants.Providers.HUAWEI, oaid, isDisabled))
        } catch (connectionException: ConnectionException) {
            val message = connectionException.message ?: "unknown exception during binding huawei services"
            getProviderUnavailableResult(message)
        } catch (e: Throwable) {
            DebugLogger.error(tag, e, "can't fetch adv id.")
            getProviderUnavailableResult("exception while fetching hoaid: " + e.message)
        } finally {
            try {
                connectionController.disconnect(context)
            } catch (ex: Throwable) {
                DebugLogger.error(tag, ex, "could not unbind from service")
            }
        }
    }
}

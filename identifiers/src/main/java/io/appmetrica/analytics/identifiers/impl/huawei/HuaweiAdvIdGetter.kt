package io.appmetrica.analytics.identifiers.impl.huawei

import android.content.Context
import android.content.Intent
import android.os.RemoteException
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvIdServiceCommunicationException
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.identifiers.impl.AdvIdInfo
import io.appmetrica.analytics.identifiers.impl.AdvIdProvider
import io.appmetrica.analytics.identifiers.impl.AdvIdResult
import io.appmetrica.analytics.identifiers.impl.AdvIdServiceConnectionController
import io.appmetrica.analytics.identifiers.impl.Constants
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
            tryToGetAdTrackingInfo(context)
        } catch (exception: RemoteException) {
            throw AdvIdServiceCommunicationException(
                "communication with huawei service failed",
                exception
            )
        } finally {
            try {
                connectionController.disconnect(context)
            } catch (ex: Throwable) {
                DebugLogger.error(tag, ex, "could not unbind from service")
            }
        }
    }

    private fun tryToGetAdTrackingInfo(context: Context): AdvIdResult {
        val service: OpenDeviceIdentifierService = connectionController.connect(context)
        val oaid = service.oaid
        DebugLogger.info(tag, "id fetched successfully: %s", oaid)
        val isDisabled = service.isOaidTrackLimited
        DebugLogger.info(tag, "mLimitedAdvertisingTracking flag fetched successfully: %b", isDisabled)
        return AdvIdResult(IdentifierStatus.OK, AdvIdInfo(Constants.Providers.HUAWEI, oaid, isDisabled))
    }
}

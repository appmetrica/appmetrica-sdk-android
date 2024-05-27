package io.appmetrica.analytics.identifiers.impl

import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.yandex.android.advid.service.YandexAdvIdInterface
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

private const val TAG = "[YandexAdvIdGetter]"
private val YANDEX_ADV_ID_INTENT =
    Intent("com.yandex.android.advid.IDENTIFIER_SERVICE").setPackage("com.yandex.android.advid")

internal class YandexAdvIdGetter @VisibleForTesting internal constructor(
    private val connectionController: AdvIdServiceConnectionController<YandexAdvIdInterface>
) : AdvIdProvider {

    constructor() : this(
        AdvIdServiceConnectionController<YandexAdvIdInterface>(
            YANDEX_ADV_ID_INTENT,
            { YandexAdvIdInterface.Stub.asInterface(it) },
            "yandex",
        )
    )

    override fun getAdTrackingInfo(context: Context): AdvIdResult {
        DebugLogger.info(TAG, "getAdTrackingInfo. Connecting to service...")
        return try {
            tryToGetAdTrackingInfo(context)
        } catch (noProviderException: NoProviderException) {
            val message = noProviderException.message ?: "No yandex adv_id service"
            DebugLogger.error(TAG, noProviderException, message)
            AdvIdResult(IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, errorExplanation = message)
        } catch (connectionException: ConnectionException) {
            val message = connectionException.message ?: "unknown exception while binding yandex adv_id service"
            DebugLogger.error(TAG, connectionException, message)
            AdvIdResult(IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, errorExplanation = message)
        } catch (e: Throwable) {
            DebugLogger.error(TAG, e, "can't fetch adv id")
            AdvIdResult(
                IdentifierStatus.UNKNOWN,
                errorExplanation = "exception while fetching yandex adv_id: " + e.message
            )
        } finally {
            try {
                connectionController.disconnect(context)
            } catch (ex: Throwable) {
                DebugLogger.error(TAG, ex, "%s could not unbind from service")
            }
        }
    }

    private fun tryToGetAdTrackingInfo(context: Context): AdvIdResult {
        val service: YandexAdvIdInterface = connectionController.connect(context)
        val advId: String = service.advId
        DebugLogger.info(TAG, "id fetched successfully: %s", advId)
        val isDisabled: Boolean = service.isAdvIdTrackingLimited
        DebugLogger.info(TAG, "limitedAdvertisingTracking flag fetched successfully: %b", isDisabled)
        return AdvIdResult(IdentifierStatus.OK, AdvIdInfo(Constants.Providers.YANDEX, advId, isDisabled))
    }
}

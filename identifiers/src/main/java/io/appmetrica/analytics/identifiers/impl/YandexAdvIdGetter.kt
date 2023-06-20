package io.appmetrica.analytics.identifiers.impl

import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.yandex.android.advid.service.YandexAdvIdInterface
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

private const val TAG = "[YandexAdvIdGetter]"
private val YANDEX_ADV_ID_INTENT =
    Intent("com.yandex.android.advid.IDENTIFIER_SERVICE").setPackage("com.yandex.android.advid")

internal class YandexAdvIdGetter @VisibleForTesting internal constructor(
    private val connectionController: AdIdServiceConnectionController<YandexAdvIdInterface>
) : AdvIdProvider {

    constructor() : this(
        AdIdServiceConnectionController<YandexAdvIdInterface>(
            YANDEX_ADV_ID_INTENT,
            { YandexAdvIdInterface.Stub.asInterface(it) },
            "yandex",
        )
    )

    override fun getAdTrackingInfo(context: Context): AdsIdResult {
        YLogger.info(TAG, "getAdTrackingInfo. Connecting to service...")
        return try {
            tryToGetAdTrackingInfo(context)
        } catch (noProviderException: NoProviderException) {
            val message = noProviderException.message ?: "No yandex adv_id service"
            YLogger.error(TAG, noProviderException, message)
            AdsIdResult(IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, errorExplanation = message)
        } catch (connectionException: ConnectionException) {
            val message = connectionException.message ?: "unknown exception while binding yandex adv_id service"
            YLogger.error(TAG, connectionException, message)
            AdsIdResult(IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, errorExplanation = message)
        } catch (e: Throwable) {
            YLogger.error(TAG, e, "can't fetch adv id")
            AdsIdResult(
                IdentifierStatus.UNKNOWN,
                errorExplanation = "exception while fetching yandex adv_id: " + e.message
            )
        } finally {
            try {
                connectionController.disconnect(context)
            } catch (ex: Throwable) {
                YLogger.error(TAG, ex, "%s could not unbind from service")
            }
        }
    }

    private fun tryToGetAdTrackingInfo(context: Context): AdsIdResult {
        val service: YandexAdvIdInterface = connectionController.connect(context)
        val advId: String = service.advId
        YLogger.info(TAG, "id fetched successfully: %s", advId)
        val isDisabled: Boolean = service.isAdvIdTrackingLimited
        YLogger.info(TAG, "limitedAdvertisingTracking flag fetched successfully: %b", isDisabled)
        return AdsIdResult(IdentifierStatus.OK, AdsIdInfo(Constants.Providers.YANDEX, advId, isDisabled))
    }
}

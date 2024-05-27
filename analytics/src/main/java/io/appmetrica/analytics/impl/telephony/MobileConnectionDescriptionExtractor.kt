package io.appmetrica.analytics.impl.telephony

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.coreutils.internal.cache.CachedDataProvider
import io.appmetrica.analytics.coreutils.internal.permission.AlwaysAllowPermissionStrategy
import io.appmetrica.analytics.coreutils.internal.permission.SinglePermissionStrategy
import io.appmetrica.analytics.coreutils.internal.services.telephony.CellularNetworkTypeExtractor
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.TimeUnit

private const val TAG = "[MobileConnectionDescriptionExtractor]"

internal class MobileConnectionDescriptionExtractor(
    private val context: Context
) : TelephonyInfoExtractor<MobileConnectionDescription> {

    private val networkTypePermissionStrategy = if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)) {
        SinglePermissionStrategy(
            GlobalServiceLocator.getInstance().generalPermissionExtractor,
            Manifest.permission.READ_PHONE_STATE
        )
    } else {
        AlwaysAllowPermissionStrategy()
    }

    private val cellularNetworkTypeExtractor = CellularNetworkTypeExtractor(context)

    private val cacheExpiryTime = TimeUnit.SECONDS.toMillis(20)

    private val cachedData = CachedDataProvider.CachedData<MobileConnectionDescription>(
        cacheExpiryTime,
        cacheExpiryTime * 2,
        "mobile-connection"
    )

    @Synchronized
    override fun extract(): MobileConnectionDescription {
        var data = cachedData.data
        if (data == null || cachedData.shouldUpdateData()) {
            data = extractInternal()
            cachedData.data = data
        }
        DebugLogger.info(TAG, "Extract returns $data")
        return data
    }

    @SuppressLint("MissingPermission")
    private fun extractInternal(): MobileConnectionDescription = MobileConnectionDescription(
        if (networkTypePermissionStrategy.hasNecessaryPermissions(context)) {
            cellularNetworkTypeExtractor.getNetworkType()
        } else {
            CellularNetworkTypeExtractor.UNKNOWN_NETWORK_TYPE_VALUE
        }
    )
}

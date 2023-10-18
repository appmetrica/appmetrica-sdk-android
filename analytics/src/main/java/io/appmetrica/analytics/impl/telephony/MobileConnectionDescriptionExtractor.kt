package io.appmetrica.analytics.impl.telephony

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import android.util.SparseArray
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.coreutils.internal.cache.CachedDataProvider
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.coreutils.internal.permission.AlwaysAllowPermissionStrategy
import io.appmetrica.analytics.coreutils.internal.permission.SinglePermissionStrategy
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils
import io.appmetrica.analytics.impl.GlobalServiceLocator
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

    @SuppressLint("InlineApi")
    private val networkTypeToStringMapping = SparseArray<String?>().apply {
        put(TelephonyManager.NETWORK_TYPE_UNKNOWN, null)
        put(TelephonyManager.NETWORK_TYPE_1xRTT, "1xRTT")
        put(TelephonyManager.NETWORK_TYPE_CDMA, "CDMA")
        put(TelephonyManager.NETWORK_TYPE_EDGE, "EDGE")
        put(TelephonyManager.NETWORK_TYPE_EHRPD, "eHRPD")
        put(TelephonyManager.NETWORK_TYPE_EVDO_0, "EVDO rev.0")
        put(TelephonyManager.NETWORK_TYPE_EVDO_A, "EVDO rev.A")
        put(TelephonyManager.NETWORK_TYPE_EVDO_B, "EVDO rev.B")
        put(TelephonyManager.NETWORK_TYPE_GPRS, "GPRS")
        put(TelephonyManager.NETWORK_TYPE_HSDPA, "HSDPA")
        put(TelephonyManager.NETWORK_TYPE_HSPA, "HSPA")
        put(TelephonyManager.NETWORK_TYPE_HSPAP, "HSPA+")
        put(TelephonyManager.NETWORK_TYPE_HSUPA, "HSUPA")
        @Suppress("DEPRECATION")
        put(TelephonyManager.NETWORK_TYPE_IDEN, "iDen")
        put(TelephonyManager.NETWORK_TYPE_UMTS, "UMTS")
        put(TelephonyManager.NETWORK_TYPE_EVDO_B, "EVDO rev.B")
        put(TelephonyManager.NETWORK_TYPE_EHRPD, "eHRPD")
        put(TelephonyManager.NETWORK_TYPE_LTE, "LTE")
        put(TelephonyManager.NETWORK_TYPE_HSPAP, "HSPA+")
    }

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
        YLogger.info(TAG, "Extract returns $data")
        return data
    }

    private fun extractInternal(): MobileConnectionDescription = MobileConnectionDescription(networkType())

    @SuppressLint("MissingPermission")
    private fun networkType(): String {
        var networkType = "unknown"
        val networkTypeFromSystem = extractFromTelephonyManager(
            { telephonyManager ->
                if (networkTypePermissionStrategy.hasNecessaryPermissions(context)) {
                    networkTypeToStringMapping[telephonyManager.networkType]
                } else {
                    null
                }
            },
            "getting networkType"
        )
        if (networkTypeFromSystem != null) {
            networkType = networkTypeFromSystem
        }
        return networkType
    }

    private fun <T> extractFromTelephonyManager(
        extractor: FunctionWithThrowable<TelephonyManager, T?>,
        description: String
    ): T? {
        return SystemServiceUtils.accessSystemServiceByNameSafely(
            context,
            Context.TELEPHONY_SERVICE,
            description,
            "TelephonyManager",
            extractor
        )
    }
}

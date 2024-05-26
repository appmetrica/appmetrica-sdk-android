package io.appmetrica.analytics.coreutils.internal.services.telephony

import android.os.Build
import android.telephony.TelephonyManager
import android.util.SparseArray
import io.appmetrica.analytics.coreutils.internal.AndroidUtils

internal object CellularNetworkTypeConverter {

    private const val UNKNOWN_NETWORK_TYPE_VALUE = CellularNetworkTypeExtractor.UNKNOWN_NETWORK_TYPE_VALUE

    private val networkTypeToStringMapping = SparseArray<String?>().apply {
        put(TelephonyManager.NETWORK_TYPE_UNKNOWN, UNKNOWN_NETWORK_TYPE_VALUE)
        put(TelephonyManager.NETWORK_TYPE_1xRTT, "1xRTT")
        put(TelephonyManager.NETWORK_TYPE_CDMA, "CDMA")
        put(TelephonyManager.NETWORK_TYPE_EDGE, "EDGE")
        put(TelephonyManager.NETWORK_TYPE_EHRPD, "eHRPD")
        put(TelephonyManager.NETWORK_TYPE_EVDO_0, "EVDO rev.0")
        put(TelephonyManager.NETWORK_TYPE_EVDO_A, "EVDO rev.A")
        put(TelephonyManager.NETWORK_TYPE_GPRS, "GPRS")
        put(TelephonyManager.NETWORK_TYPE_HSDPA, "HSDPA")
        put(TelephonyManager.NETWORK_TYPE_HSPA, "HSPA")
        put(TelephonyManager.NETWORK_TYPE_HSPAP, "HSPA+")
        put(TelephonyManager.NETWORK_TYPE_HSUPA, "HSUPA")
        @Suppress("DEPRECATION")
        put(TelephonyManager.NETWORK_TYPE_IDEN, "iDen")
        put(TelephonyManager.NETWORK_TYPE_UMTS, "UMTS")
        put(TelephonyManager.NETWORK_TYPE_LTE, "LTE")
        put(TelephonyManager.NETWORK_TYPE_GSM, "GSM")
        put(TelephonyManager.NETWORK_TYPE_TD_SCDMA, "TD_SCDMA")
        put(TelephonyManager.NETWORK_TYPE_IWLAN, "IWLAN")
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)) {
            put(TelephonyManager.NETWORK_TYPE_NR, "NR")
        }
    }

    @JvmStatic
    fun convert(systemValue: Int?): String =
        systemValue?.let { networkTypeToStringMapping[systemValue] } ?: UNKNOWN_NETWORK_TYPE_VALUE
}

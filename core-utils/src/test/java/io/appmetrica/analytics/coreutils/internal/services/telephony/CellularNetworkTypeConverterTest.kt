package io.appmetrica.analytics.coreutils.internal.services.telephony

import android.telephony.TelephonyManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters

@RunWith(ParameterizedRobolectricTestRunner::class)
class CellularNetworkTypeConverterTest(val input: Int?, val expected: String) {

    companion object {

        @JvmStatic
        @Parameters(name = "{0} -> {1}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(null, "unknown"),
            arrayOf(100500, "unknown"),
            arrayOf(TelephonyManager.NETWORK_TYPE_UNKNOWN, "unknown"),
            arrayOf(TelephonyManager.NETWORK_TYPE_1xRTT, "1xRTT"),
            arrayOf(TelephonyManager.NETWORK_TYPE_CDMA, "CDMA"),
            arrayOf(TelephonyManager.NETWORK_TYPE_EDGE, "EDGE"),
            arrayOf(TelephonyManager.NETWORK_TYPE_EHRPD, "eHRPD"),
            arrayOf(TelephonyManager.NETWORK_TYPE_EVDO_0, "EVDO rev.0"),
            arrayOf(TelephonyManager.NETWORK_TYPE_EVDO_A, "EVDO rev.A"),
            arrayOf(TelephonyManager.NETWORK_TYPE_GPRS, "GPRS"),
            arrayOf(TelephonyManager.NETWORK_TYPE_HSDPA, "HSDPA"),
            arrayOf(TelephonyManager.NETWORK_TYPE_HSPA, "HSPA"),
            arrayOf(TelephonyManager.NETWORK_TYPE_HSPAP, "HSPA+"),
            arrayOf(TelephonyManager.NETWORK_TYPE_HSUPA, "HSUPA"),
            arrayOf(TelephonyManager.NETWORK_TYPE_IDEN, "iDen"),
            arrayOf(TelephonyManager.NETWORK_TYPE_UMTS, "UMTS"),
            arrayOf(TelephonyManager.NETWORK_TYPE_LTE, "LTE"),
            arrayOf(TelephonyManager.NETWORK_TYPE_GSM, "GSM"),
            arrayOf(TelephonyManager.NETWORK_TYPE_TD_SCDMA, "TD_SCDMA"),
            arrayOf(TelephonyManager.NETWORK_TYPE_IWLAN, "IWLAN"),
            arrayOf(TelephonyManager.NETWORK_TYPE_NR, "NR")
        )
    }

    @Test
    fun convert() {
        assertThat(CellularNetworkTypeConverter.convert(input)).isEqualTo(expected)
    }
}

package io.appmetrica.analytics.coreutils.internal.services.telephony

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import io.appmetrica.analytics.coreapi.internal.annotations.DoNotInline
import io.appmetrica.analytics.coreapi.internal.backport.FunctionWithThrowable
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils

class CellularNetworkTypeExtractor(val context: Context) {

    companion object {
        const val UNKNOWN_NETWORK_TYPE_VALUE = "unknown"
    }

    @Suppress("DEPRECATION")
    @DoNotInline
    private class ExtractorPreN : FunctionWithThrowable<TelephonyManager, Int?> {
        @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
        override fun apply(input: TelephonyManager): Int = input.networkType
    }

    @TargetApi(Build.VERSION_CODES.N)
    @DoNotInline
    private class ExtractorN : FunctionWithThrowable<TelephonyManager, Int?> {
        @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
        override fun apply(input: TelephonyManager): Int = input.dataNetworkType
    }

    private val extractor: FunctionWithThrowable<TelephonyManager, Int?> =
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.N)) {
            ExtractorN()
        } else {
            ExtractorPreN()
        }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getNetworkType(): String = CellularNetworkTypeConverter.convert(
        SystemServiceUtils.accessSystemServiceByNameSafely(
            context,
            Context.TELEPHONY_SERVICE,
            "Extracting cellular networkType",
            "TelephonyManager",
            extractor
        )
    )
}

package io.appmetrica.analytics.impl.telephony

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import io.appmetrica.analytics.coreapi.internal.annotations.DoNotInline
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.coreutils.internal.system.SystemServiceUtils

@DoNotInline
@TargetApi(Build.VERSION_CODES.M)
internal object SimInfoExtractorForM {

    @SuppressWarnings("MissingPermission")
    @JvmStatic
    fun extractSimInfosFromSubscriptionManager(context: Context): List<SimInfo> {
        return SystemServiceUtils.accessSystemServiceByNameSafely<SubscriptionManager, List<SubscriptionInfo>?>(
            context,
            Context.TELEPHONY_SUBSCRIPTION_SERVICE,
            "getting active subcription info list",
            "SubscriptionManager",
        ) { it.activeSubscriptionInfoList }?.map { it ->
            SimInfo(
                simCountryCode = if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)) {
                    SimInfoHelperForQ.mobileCountryCode(it)
                } else {
                    it.mcc
                },
                simNetworkCode = if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.Q)) {
                    SimInfoHelperForQ.mobileNetworkCode(it)
                } else {
                    it.mnc
                },
                isNetworkRoaming = it.dataRoaming == SubscriptionManager.DATA_ROAMING_ENABLE,
                operatorName = it.carrierName?.toString(),
            )
        } ?: emptyList()
    }
}

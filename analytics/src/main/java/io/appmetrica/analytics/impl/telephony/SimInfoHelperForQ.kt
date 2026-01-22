package io.appmetrica.analytics.impl.telephony

import android.annotation.TargetApi
import android.os.Build
import android.telephony.SubscriptionInfo
import io.appmetrica.analytics.coreapi.internal.annotations.DoNotInline
import io.appmetrica.analytics.coreutils.internal.parsing.ParseUtils

@DoNotInline
@TargetApi(Build.VERSION_CODES.Q)
internal object SimInfoHelperForQ {
    @JvmStatic
    fun mobileCountryCode(subscriptionInfo: SubscriptionInfo): Int? =
        ParseUtils.intValueOf(subscriptionInfo.mccString)

    @JvmStatic
    fun mobileNetworkCode(subscriptionInfo: SubscriptionInfo): Int? =
        ParseUtils.intValueOf(subscriptionInfo.mncString)
}

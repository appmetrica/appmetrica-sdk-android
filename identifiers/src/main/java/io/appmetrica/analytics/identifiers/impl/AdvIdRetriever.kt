package io.appmetrica.analytics.identifiers.impl

import android.content.Context
import android.os.Bundle
import io.appmetrica.analytics.identifiers.impl.huawei.HuaweiAdvIdGetter

internal class AdvIdRetriever(
    private val providers: Map<String, AdvIdProvider> = mapOf(
        Constants.Providers.GOOGLE to GoogleAdvIdGetter(),
        Constants.Providers.HUAWEI to HuaweiAdvIdGetter(),
        Constants.Providers.YANDEX to YandexAdvIdGetter(),
    )
) {

    fun requestId(context: Context, provider: String?): Bundle? =
        providers[provider]?.getAdTrackingInfo(context)?.toBundle()
}

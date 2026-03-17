package io.appmetrica.analytics.impl.referrer.service.provider.huawei

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.provider.ReferrerProvider
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class HuaweiReferrerProvider(
    private val context: Context,
) : ReferrerProvider {
    private val tag = "[HuaweiReferrerProvider]"

    override val referrerName: String = "huawei"

    override fun requestReferrer(listener: ReferrerListener) {
        DebugLogger.info(tag, "Try to get $referrerName referrer")
        GlobalServiceLocator.getInstance().serviceExecutorProvider.getHmsReferrerThread {
            listener.onResult(HuaweiReferrerContentProvider().getReferrer(context))
        }.start()
    }
}

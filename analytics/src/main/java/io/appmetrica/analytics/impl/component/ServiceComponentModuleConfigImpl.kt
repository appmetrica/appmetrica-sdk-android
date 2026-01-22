package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceComponentModuleConfig
import io.appmetrica.analytics.coreutils.internal.WrapUtils
import io.appmetrica.analytics.impl.DefaultValues

internal class ServiceComponentModuleConfigImpl(
    private val config: CommonArguments.ReporterArguments,
) : ServiceComponentModuleConfig {

    override fun isRevenueAutoTrackingEnabled(): Boolean = WrapUtils.getOrDefault(
        config.revenueAutoTrackingEnabled,
        DefaultValues.DEFAULT_REVENUE_AUTO_TRACKING_ENABLED
    )
}

package io.appmetrica.analytics.impl.adrevenue

import io.appmetrica.analytics.ModulesFacade
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenue
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenueReporter

class AppMetricaAutoAdRevenueReporter : AutoAdRevenueReporter {

    private val autoAdRevenueConverter = AutoAdRevenueConverter()

    override fun reportAutoAdRevenue(autoAdRevenue: AutoAdRevenue) {
        ModulesFacade.reportAdRevenue(autoAdRevenueConverter.convert(autoAdRevenue))
    }
}

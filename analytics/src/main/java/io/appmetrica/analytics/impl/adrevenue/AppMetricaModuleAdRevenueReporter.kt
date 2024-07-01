package io.appmetrica.analytics.impl.adrevenue

import io.appmetrica.analytics.ModulesFacade
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueReporter

class AppMetricaModuleAdRevenueReporter : ModuleAdRevenueReporter {

    private val converter = ModuleAdRevenueConverter()

    override fun reportAutoAdRevenue(adRevenue: ModuleAdRevenue) {
        ModulesFacade.reportAdRevenue(converter.convert(adRevenue), adRevenue.autoCollected)
    }
}

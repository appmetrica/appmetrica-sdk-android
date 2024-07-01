package io.appmetrica.analytics.impl.modules.client.context

import io.appmetrica.analytics.impl.modules.client.CompositeModuleAdRevenueProcessor
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueContext
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueReporter

interface CoreModuleAdRevenueContext : ModuleAdRevenueContext {

    override val adRevenueReporter: ModuleAdRevenueReporter
    override val adRevenueProcessorsHolder: CompositeModuleAdRevenueProcessor
}

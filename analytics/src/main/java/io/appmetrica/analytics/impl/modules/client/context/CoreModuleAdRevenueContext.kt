package io.appmetrica.analytics.impl.modules.client.context

import io.appmetrica.analytics.impl.modules.client.CompositeModuleAdRevenueProcessor
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueContext

interface CoreModuleAdRevenueContext : ModuleAdRevenueContext {

    override val adRevenueProcessorsHolder: CompositeModuleAdRevenueProcessor
}

package io.appmetrica.analytics.impl.modules.client.context

import io.appmetrica.analytics.impl.modules.client.CompositeModuleAdRevenueProcessor
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueReporter

class CoreModuleAdRevenueContextImpl(
    override val adRevenueReporter: ModuleAdRevenueReporter,
    override val adRevenueProcessorsHolder: CompositeModuleAdRevenueProcessor
) : CoreModuleAdRevenueContext

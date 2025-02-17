package io.appmetrica.analytics.impl.modules.client.context

import io.appmetrica.analytics.impl.modules.client.CompositeModuleAdRevenueProcessor

class CoreModuleAdRevenueContextImpl(
    override val adRevenueProcessorsHolder: CompositeModuleAdRevenueProcessor
) : CoreModuleAdRevenueContext

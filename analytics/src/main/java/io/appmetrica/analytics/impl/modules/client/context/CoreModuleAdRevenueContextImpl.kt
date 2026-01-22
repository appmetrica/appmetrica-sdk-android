package io.appmetrica.analytics.impl.modules.client.context

import io.appmetrica.analytics.impl.modules.client.CompositeModuleAdRevenueProcessor

internal class CoreModuleAdRevenueContextImpl(
    override val adRevenueProcessorsHolder: CompositeModuleAdRevenueProcessor
) : CoreModuleAdRevenueContext

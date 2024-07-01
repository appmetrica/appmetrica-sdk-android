package io.appmetrica.analytics.impl.modules.client.context

import io.appmetrica.analytics.impl.adrevenue.AppMetricaModuleAdRevenueReporter
import io.appmetrica.analytics.impl.modules.client.CompositeModuleAdRevenueProcessor

class ClientContextImpl : CoreClientContext {

    override val moduleAdRevenueContext: CoreModuleAdRevenueContext =
        CoreModuleAdRevenueContextImpl(
            AppMetricaModuleAdRevenueReporter(),
            CompositeModuleAdRevenueProcessor()
        )
}

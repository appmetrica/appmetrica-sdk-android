package io.appmetrica.analytics.impl.modules.client

import io.appmetrica.analytics.modulesapi.internal.client.ClientContext
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenueReporter

class ClientContextFacade(
    override val autoAdRevenueReporter: AutoAdRevenueReporter
) : ClientContext

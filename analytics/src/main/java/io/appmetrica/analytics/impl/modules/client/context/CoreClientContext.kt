package io.appmetrica.analytics.impl.modules.client.context

import io.appmetrica.analytics.modulesapi.internal.client.ClientContext

interface CoreClientContext : ClientContext {

    override val moduleAdRevenueContext: CoreModuleAdRevenueContext
}

package io.appmetrica.analytics.impl.modules.client.context

import io.appmetrica.analytics.modulesapi.internal.client.ClientContext

internal interface CoreClientContext : ClientContext {

    override val moduleAdRevenueContext: CoreModuleAdRevenueContext
}

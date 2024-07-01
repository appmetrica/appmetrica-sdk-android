package io.appmetrica.analytics.impl.modules.client

import io.appmetrica.analytics.impl.modules.client.context.CoreClientContext

internal interface ClientSideModuleInitializer {

    fun initClientSide(clientContext: CoreClientContext)
}

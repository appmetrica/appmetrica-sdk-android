package io.appmetrica.analytics.impl.modules.client

import io.appmetrica.analytics.modulesapi.internal.client.ClientContext

internal interface ClientSideModuleInitializer {

    fun initClientSide(clientContext: ClientContext)
}

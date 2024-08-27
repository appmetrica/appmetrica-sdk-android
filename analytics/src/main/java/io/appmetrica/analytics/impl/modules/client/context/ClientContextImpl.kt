package io.appmetrica.analytics.impl.modules.client.context

import android.content.Context
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.impl.adrevenue.AppMetricaModuleAdRevenueReporter
import io.appmetrica.analytics.impl.modules.client.ClientStorageProviderImpl
import io.appmetrica.analytics.impl.modules.client.CompositeModuleAdRevenueProcessor
import io.appmetrica.analytics.modulesapi.internal.client.ClientStorageProvider

class ClientContextImpl(
    context: Context
) : CoreClientContext {

    override val moduleAdRevenueContext: CoreModuleAdRevenueContext =
        CoreModuleAdRevenueContextImpl(
            AppMetricaModuleAdRevenueReporter(),
            CompositeModuleAdRevenueProcessor()
        )

    override val clientStorageProvider: ClientStorageProvider = ClientStorageProviderImpl(
        ClientServiceLocator.getInstance().getPreferencesClientDbStorage(context)
    )
}

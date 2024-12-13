package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.MainReporterComponentId
import io.appmetrica.analytics.impl.component.RegularDispatcherComponentFactory

internal class MainReporterClientFactory : ClientUnitFactory<MainReporterClientUnit> {
    override fun createClientUnit(
        context: Context,
        repository: ComponentsRepository,
        clientDescription: ClientDescription,
        sdkConfig: CommonArguments
    ): MainReporterClientUnit {
        val componentUnit =
            repository.getOrCreateRegularComponent(
                MainReporterComponentId(clientDescription.packageName, clientDescription.apiKey),
                sdkConfig,
                RegularDispatcherComponentFactory(MainReporterComponentUnitFactory())
            )

        return MainReporterClientUnit(
            context,
            componentUnit
        )
    }
}

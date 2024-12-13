package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.IComponent
import io.appmetrica.analytics.impl.component.IReportableComponent
import io.appmetrica.analytics.impl.component.RegularDispatcherComponentFactory

internal class ReporterClientUnitFactory<COMPONENT>(
    private val componentUnitFactory: ComponentUnitFactory<COMPONENT>
) : ClientUnitFactory<RegularClientUnit>
    where COMPONENT : IReportableComponent, COMPONENT : IComponent {

    override fun createClientUnit(
        context: Context,
        repository: ComponentsRepository,
        clientDescription: ClientDescription,
        sdkConfig: CommonArguments
    ): RegularClientUnit {
        val componentUnit = repository.getOrCreateRegularComponent(
            ComponentId(clientDescription.packageName, clientDescription.apiKey),
            sdkConfig,
            RegularDispatcherComponentFactory(componentUnitFactory)
        )

        return RegularClientUnit(context, componentUnit)
    }
}

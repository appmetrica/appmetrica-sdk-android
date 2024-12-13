package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.CommutationComponentId
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponentFactory

// For [Idle/Anonymous/Commutation]ReporterEnvironment
internal class MainCommutationClientUnitFactory : ClientUnitFactory<CommutationClientUnit> {
    override fun createClientUnit(
        context: Context,
        repository: ComponentsRepository,
        clientDescription: ClientDescription,
        sdkConfig: CommonArguments
    ): CommutationClientUnit {
        val componentUnit = repository.getOrCreateCommutationComponent(
            CommutationComponentId(clientDescription.packageName),
            sdkConfig,
            CommutationDispatcherComponentFactory()
        )

        return CommutationClientUnit(
            context,
            componentUnit,
            sdkConfig
        )
    }
}

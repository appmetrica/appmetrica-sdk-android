package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ComponentId

internal class SelfDiagnosticReporterClientUnitFactory : ClientUnitFactory<SelfDiagnosticClientUnit> {
    override fun createClientUnit(
        context: Context,
        repository: ComponentsRepository,
        clientDescription: ClientDescription,
        sdkConfig: CommonArguments
    ): SelfDiagnosticClientUnit = SelfDiagnosticClientUnit(
        repository.getRegularComponentIfExists(
            ComponentId(clientDescription.packageName, clientDescription.apiKey)
        )
    )
}

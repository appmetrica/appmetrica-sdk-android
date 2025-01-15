package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ComponentEventTriggerProviderCreator
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.ReporterComponentUnit
import io.appmetrica.analytics.impl.startup.StartupUnit
import io.appmetrica.analytics.impl.startup.executor.RegularExecutorFactory

internal class ReporterComponentUnitFactory : ComponentUnitFactory<ReporterComponentUnit> {

    override fun createComponentUnit(
        context: Context,
        componentId: ComponentId,
        sdkConfig: CommonArguments.ReporterArguments,
        startupUnit: StartupUnit
    ): ReporterComponentUnit = ReporterComponentUnit(
        context,
        componentId,
        sdkConfig,
        GlobalServiceLocator.getInstance().dataSendingRestrictionController,
        startupUnit.startupState,
        RegularExecutorFactory(startupUnit),
        ComponentEventTriggerProviderCreator()
    )
}

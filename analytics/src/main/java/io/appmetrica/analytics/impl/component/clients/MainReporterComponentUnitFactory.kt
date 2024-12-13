package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.MainReporterComponentUnit
import io.appmetrica.analytics.impl.startup.StartupUnit
import io.appmetrica.analytics.impl.startup.executor.RegularExecutorFactory

internal class MainReporterComponentUnitFactory : ComponentUnitFactory<MainReporterComponentUnit> {

    override fun createComponentUnit(
        context: Context,
        componentId: ComponentId,
        sdkConfig: CommonArguments.ReporterArguments,
        startupUnit: StartupUnit
    ): MainReporterComponentUnit = MainReporterComponentUnit(
        context,
        startupUnit.startupState,
        componentId,
        sdkConfig,
        GlobalServiceLocator.getInstance().referrerHolder,
        GlobalServiceLocator.getInstance().dataSendingRestrictionController,
        RegularExecutorFactory(startupUnit)
    )
}

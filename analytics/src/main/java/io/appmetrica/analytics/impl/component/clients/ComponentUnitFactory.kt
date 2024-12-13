package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.component.CommonArguments.ReporterArguments
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.IComponent
import io.appmetrica.analytics.impl.component.IReportableComponent
import io.appmetrica.analytics.impl.startup.StartupUnit

internal interface ComponentUnitFactory<C> where C : IReportableComponent, C : IComponent {
    fun createComponentUnit(
        context: Context,
        componentId: ComponentId,
        sdkConfig: ReporterArguments,
        startupUnit: StartupUnit
    ): C
}

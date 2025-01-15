package io.appmetrica.analytics.impl.component.clients

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ComponentEventTriggerProviderCreator
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.SelfReportingArgumentsFactory
import io.appmetrica.analytics.impl.component.SelfSdkReportingComponentUnit
import io.appmetrica.analytics.impl.startup.StartupUnit
import io.appmetrica.analytics.impl.startup.executor.StubbedExecutorFactory

internal class SelfSdkReporterComponentUnitFactory : ComponentUnitFactory<SelfSdkReportingComponentUnit> {

    override fun createComponentUnit(
        context: Context,
        componentId: ComponentId,
        sdkConfig: CommonArguments.ReporterArguments,
        startupUnit: StartupUnit
    ): SelfSdkReportingComponentUnit = SelfSdkReportingComponentUnit(
        context,
        startupUnit.startupState,
        componentId,
        sdkConfig,
        SelfReportingArgumentsFactory(
            GlobalServiceLocator.getInstance().dataSendingRestrictionController
        ),
        StubbedExecutorFactory(),
        ComponentEventTriggerProviderCreator()
    )
}

package io.appmetrica.analytics.impl.component

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.component.CommonArguments.ReporterArguments
import io.appmetrica.analytics.impl.component.clients.ClientUnit
import io.appmetrica.analytics.impl.component.clients.ComponentUnitFactory
import io.appmetrica.analytics.impl.startup.StartupCenter
import io.appmetrica.analytics.impl.startup.StartupError
import io.appmetrica.analytics.impl.startup.StartupListener
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.impl.startup.StartupUnit

internal class RegularDispatcherComponent<COMPONENT>(
    @get:VisibleForTesting val context: Context,
    @get:VisibleForTesting val componentId: ComponentId,
    sdkConfig: CommonArguments,
    componentUnitFactory: ComponentUnitFactory<COMPONENT>,
    private val lifecycleManager: ComponentLifecycleManager<ClientUnit?>,
    startupCenter: StartupCenter
) : IClientConsumer<ClientUnit?>,
    StartupListener,
    IDispatcherComponent where COMPONENT : IReportableComponent, COMPONENT : IComponent {

    constructor(
        context: Context,
        componentId: ComponentId,
        sdkConfig: CommonArguments,
        componentUnitFactory: ComponentUnitFactory<COMPONENT>
    ) : this(
        context,
        componentId,
        sdkConfig,
        componentUnitFactory,
        ComponentLifecycleManager<ClientUnit?>(),
        StartupCenter.getInstance()
    )

    private val tag = "[RegularDispatcherComponent]"

    private val startupUnit: StartupUnit =
        startupCenter.getOrCreateStartupUnit(context, componentId, sdkConfig.startupArguments)

    private val reportingComponent: COMPONENT = componentUnitFactory.createComponentUnit(
        context,
        componentId,
        sdkConfig.componentArguments,
        startupUnit
    )

    init {
        startupCenter.registerStartupListener(componentId, this)
    }

    fun handleReport(counterReport: CounterReport, configuration: CommonArguments) {
        YLogger.d("%s handle report for componentId: %s; data: %s", tag, componentId, counterReport)

        if (!EventsManager.isEventWithoutAppConfigUpdate(counterReport.type)) {
            updateComponentConfig(configuration.componentArguments)
        }

        reportingComponent.handleReport(counterReport)
    }

    private fun updateComponentConfig(sdkConfig: ReporterArguments) {
        YLogger.d("%sUpdate sdk config for componentId: %s; config: %s", tag, componentId, sdkConfig)
        reportingComponent.updateSdkConfig(sdkConfig)
    }

    override fun connectClient(client: ClientUnit) {
        lifecycleManager.connectClient(client)
    }

    override fun disconnectClient(client: ClientUnit) {
        lifecycleManager.disconnectClient(client)
    }

    override fun onStartupChanged(newState: StartupState) {
        reportingComponent.onStartupChanged(newState)
    }

    override fun onStartupError(error: StartupError, existingState: StartupState?) {
        reportingComponent.onStartupError(error, existingState)
    }

    override fun updateConfig(arguments: CommonArguments) {
        startupUnit.updateConfiguration(arguments.startupArguments)
        updateComponentConfig(arguments.componentArguments)
    }
}

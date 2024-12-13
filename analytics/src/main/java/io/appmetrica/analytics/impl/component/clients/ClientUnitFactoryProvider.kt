package io.appmetrica.analytics.impl.component.clients

import io.appmetrica.analytics.internal.CounterConfigurationReporterType

internal class ClientUnitFactoryProvider {

    fun getClientUnitFactory(clientDescription: ClientDescription): ClientUnitFactory<*> {
        val factory = when (clientDescription.reporterType) {
            CounterConfigurationReporterType.COMMUTATION -> MainCommutationClientUnitFactory()
            CounterConfigurationReporterType.SELF_DIAGNOSTIC_MAIN -> SelfDiagnosticMainClientUnitFactory()
            CounterConfigurationReporterType.SELF_DIAGNOSTIC_MANUAL -> SelfDiagnosticReporterClientUnitFactory()
            CounterConfigurationReporterType.MANUAL -> ReporterClientUnitFactory(ReporterComponentUnitFactory())
            CounterConfigurationReporterType.SELF_SDK ->
                ReporterClientUnitFactory(SelfSdkReporterComponentUnitFactory())

            CounterConfigurationReporterType.MAIN -> MainReporterClientFactory()
            CounterConfigurationReporterType.CRASH -> MainReporterClientFactory()
        }
        return factory
    }
}

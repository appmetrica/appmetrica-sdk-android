package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleReporterComponentContext

internal class ServiceModuleReporterComponentContextImpl(
    componentUnit: ComponentUnit,
    config: CommonArguments.ReporterArguments,
) : ServiceModuleReporterComponentContext {

    override val reporter = ServiceComponentModuleReporterImpl(
        componentUnit = componentUnit,
    )

    override val config = ServiceComponentModuleConfigImpl(
        config = config,
    )
}

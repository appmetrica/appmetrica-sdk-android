package io.appmetrica.analytics.coreapi.internal.servicecomponents

interface ServiceModuleReporterComponentContext {

    val reporter: ServiceComponentModuleReporter

    val config: ServiceComponentModuleConfig
}

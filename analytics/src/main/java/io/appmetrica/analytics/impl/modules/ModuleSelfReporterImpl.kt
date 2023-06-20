package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.modulesapi.internal.ModuleSelfReporter

class ModuleSelfReporterImpl : ModuleSelfReporter {

    private val reporter = AppMetricaSelfReportFacade.getReporter()

    override fun reportEvent(eventName: String) = reporter.reportEvent(eventName)

    override fun reportEvent(eventName: String, eventValue: Map<String, Any>?) =
        reporter.reportEvent(eventName, eventValue)

    override fun reportEvent(eventName: String, eventValue: String?) = reporter.reportEvent(eventName, eventValue)

    override fun reportEvent(type: Int, eventName: String, eventValue: String?) {
        reporter.reportEvent(
            ModuleEvent.newBuilder(type)
                .withName(eventName)
                .withValue(eventValue)
                .build()
        )
    }

    override fun reportError(message: String, error: Throwable?) = reporter.reportError(message, error)

    override fun reportError(identifier: String, message: String?) = reporter.reportError(identifier, message)
}

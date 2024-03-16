package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.modulesapi.internal.common.ModuleSelfReporter

class ModuleSelfReporterImpl : ModuleSelfReporter {

    private val reporter = AppMetricaSelfReportFacade.getReporter()
    private val defaultEventType = EventProto.ReportMessage.Session.Event.EVENT_CLIENT

    override fun reportEvent(eventName: String) {
        reporter.reportEvent(ModuleEvent.newBuilder(defaultEventType).withName(eventName).build())
    }

    override fun reportEvent(eventName: String, eventValue: Map<String, Any>?) {
        reporter.reportEvent(
            ModuleEvent.newBuilder(defaultEventType)
                .withName(eventName)
                .withAttributes(eventValue)
                .build()
        )
    }

    override fun reportEvent(eventName: String, eventValue: String?) {
        reporter.reportEvent(
            ModuleEvent.newBuilder(defaultEventType)
                .withName(eventName)
                .withValue(eventValue)
                .build()
        )
    }

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

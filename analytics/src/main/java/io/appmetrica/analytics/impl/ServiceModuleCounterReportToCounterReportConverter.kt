package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreapi.internal.servicecomponents.ServiceModuleCounterReport

class ServiceModuleCounterReportToCounterReportConverter {

    fun convert(report: ServiceModuleCounterReport): CounterReport {
        return CounterReport().apply {
            type = report.type
            report.name?.let { name = it }
            if (report.valueBytes == null) {
                report.value?.let { value = it }
            }
            report.valueBytes?.let { valueBytes = it }
        }
    }
}

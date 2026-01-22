package io.appmetrica.analytics.impl.service

internal class ServiceDataReporterHolder {

    private val serviceDataReporters = mutableMapOf<Int, MutableList<ServiceDataReporter>>()

    fun getServiceDataReporters(type: Int): List<ServiceDataReporter> {
        return serviceDataReporters[type] ?: emptyList()
    }

    fun registerServiceDataReporter(type: Int, reporter: ServiceDataReporter) {
        serviceDataReporters.getOrPut(type, ::mutableListOf).add(reporter)
    }
}

package io.appmetrica.analytics

import io.appmetrica.analytics.impl.service.MetricaServiceDataReporter

class ModuleEvent(
    val type: Int,
    val name: String?,
    val value: String?,
    val metricaServiceDataReporterType: Int,
    val environment: Map<String, Any?>?,
    val extras: Map<String, ByteArray>?,
    val attributes: Map<String, Any?>?
) {

    companion object {

        @JvmStatic
        fun newBuilder(type: Int) = Builder(type)
    }

    class Builder(
        private val type: Int
    ) {

        private var name: String? = null
        private var value: String? = null
        private var metricaServiceDataReporterType: Int = MetricaServiceDataReporter.TYPE_CORE
        private var environment: Map<String, Any?>? = null
        private var extras: Map<String, ByteArray>? = null
        private var attributes: Map<String, Any>? = null

        fun withName(name: String?) = apply {
            this.name = name
        }

        fun withValue(value: String?) = apply {
            this.value = value
        }

        fun withMetricaServiceDataReporterType(metricaServiceDataReporterType: Int) = apply {
            this.metricaServiceDataReporterType = metricaServiceDataReporterType
        }

        fun withEnvironment(environment: Map<String, Any?>?) = apply {
            this.environment = environment?.toMap()
        }

        fun withExtras(extras: Map<String, ByteArray>?) = apply {
            this.extras = extras?.toMap()
        }

        fun withAttributes(attributes: Map<String, Any>?) = apply {
            this.attributes = attributes?.toMap()
        }

        fun build() = ModuleEvent(
            type,
            name,
            value,
            metricaServiceDataReporterType,
            environment,
            extras,
            attributes
        )
    }

    override fun toString(): String {
        return "ModuleEvent(" +
            "type=$type, " +
            "name=$name, " +
            "value=$value, " +
            "metricaServiceDataReporterType=$metricaServiceDataReporterType, " +
            "environment=$environment, " +
            "extras=$extras, " +
            "attributes=$attributes" +
            ")"
    }
}

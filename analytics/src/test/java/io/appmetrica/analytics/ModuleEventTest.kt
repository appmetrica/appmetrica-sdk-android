package io.appmetrica.analytics

import io.appmetrica.analytics.impl.service.MetricaServiceDataReporter
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ModuleEventTest {

    private val type = 42
    private val name = "some name"
    private val value = "some value"
    private val serviceDataReporterType = 4242
    private val environment = mapOf(
        "first environment key" to "first enviroment value",
        "second environment key" to "second environment value"
    )
    private val extras = mapOf(
        "first extras key" to ByteArray(5) { pos -> pos.toByte() },
        "second extras key" to ByteArray(4) { pos -> pos.toByte() }
    )
    private val attributes = mapOf(
        "first attributes key" to "some string",
        "second attributes key" to ByteArray(4) { pos -> pos.toByte() }
    )

    @Test
    fun onlyType() {
        val moduleEvent = ModuleEvent.newBuilder(type)
            .build()

        SoftAssertions().apply {
            assertThat(moduleEvent.type).isEqualTo(type)
            assertThat(moduleEvent.name).isNull()
            assertThat(moduleEvent.value).isNull()
            assertThat(moduleEvent.serviceDataReporterType).isEqualTo(MetricaServiceDataReporter.TYPE_CORE)
            assertThat(moduleEvent.environment).isNull()
            assertThat(moduleEvent.extras).isNull()
            assertThat(moduleEvent.attributes).isNull()
        }.assertAll()
    }

    @Test
    fun withName() {
        val moduleEvent = ModuleEvent.newBuilder(type)
            .withName(name)
            .build()

        SoftAssertions().apply {
            assertThat(moduleEvent.type).isEqualTo(type)
            assertThat(moduleEvent.name).isEqualTo(name)
            assertThat(moduleEvent.value).isNull()
            assertThat(moduleEvent.serviceDataReporterType).isEqualTo(MetricaServiceDataReporter.TYPE_CORE)
            assertThat(moduleEvent.environment).isNull()
            assertThat(moduleEvent.extras).isNull()
            assertThat(moduleEvent.attributes).isNull()
        }.assertAll()
    }

    @Test
    fun withValue() {
        val moduleEvent = ModuleEvent.newBuilder(type)
            .withValue(value)
            .build()

        SoftAssertions().apply {
            assertThat(moduleEvent.type).isEqualTo(type)
            assertThat(moduleEvent.name).isNull()
            assertThat(moduleEvent.value).isEqualTo(value)
            assertThat(moduleEvent.serviceDataReporterType).isEqualTo(MetricaServiceDataReporter.TYPE_CORE)
            assertThat(moduleEvent.environment).isNull()
            assertThat(moduleEvent.extras).isNull()
            assertThat(moduleEvent.attributes).isNull()
        }.assertAll()
    }

    @Test
    fun withServiceDataReporterType() {
        val moduleEvent = ModuleEvent.newBuilder(type)
            .withServiceDataReporterType(serviceDataReporterType)
            .build()

        SoftAssertions().apply {
            assertThat(moduleEvent.type).isEqualTo(type)
            assertThat(moduleEvent.name).isNull()
            assertThat(moduleEvent.value).isNull()
            assertThat(moduleEvent.serviceDataReporterType).isEqualTo(serviceDataReporterType)
            assertThat(moduleEvent.environment).isNull()
            assertThat(moduleEvent.extras).isNull()
            assertThat(moduleEvent.attributes).isNull()
        }.assertAll()
    }

    @Test
    fun withEnvironment() {
        val moduleEvent = ModuleEvent.newBuilder(type)
            .withEnvironment(environment)
            .build()

        SoftAssertions().apply {
            assertThat(moduleEvent.type).isEqualTo(type)
            assertThat(moduleEvent.name).isNull()
            assertThat(moduleEvent.value).isNull()
            assertThat(moduleEvent.serviceDataReporterType).isEqualTo(MetricaServiceDataReporter.TYPE_CORE)
            assertThat(moduleEvent.environment).isEqualTo(environment)
            assertThat(moduleEvent.extras).isNull()
            assertThat(moduleEvent.attributes).isNull()
        }.assertAll()
    }

    @Test
    fun withExtras() {
        val moduleEvent = ModuleEvent.newBuilder(type)
            .withExtras(extras)
            .build()

        SoftAssertions().apply {
            assertThat(moduleEvent.type).isEqualTo(type)
            assertThat(moduleEvent.name).isNull()
            assertThat(moduleEvent.value).isNull()
            assertThat(moduleEvent.serviceDataReporterType).isEqualTo(MetricaServiceDataReporter.TYPE_CORE)
            assertThat(moduleEvent.environment).isNull()
            assertThat(moduleEvent.extras).isEqualTo(extras)
            assertThat(moduleEvent.attributes).isNull()
        }.assertAll()
    }

    @Test
    fun withAttributes() {
        val moduleEvent = ModuleEvent.newBuilder(type)
            .withAttributes(attributes)
            .build()

        SoftAssertions().apply {
            assertThat(moduleEvent.type).isEqualTo(type)
            assertThat(moduleEvent.name).isNull()
            assertThat(moduleEvent.value).isNull()
            assertThat(moduleEvent.serviceDataReporterType).isEqualTo(MetricaServiceDataReporter.TYPE_CORE)
            assertThat(moduleEvent.environment).isNull()
            assertThat(moduleEvent.extras).isNull()
            assertThat(moduleEvent.attributes).isEqualTo(attributes)
        }.assertAll()
    }
}

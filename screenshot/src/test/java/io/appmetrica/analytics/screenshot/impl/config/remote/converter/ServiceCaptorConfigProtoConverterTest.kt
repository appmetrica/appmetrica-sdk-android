package io.appmetrica.analytics.screenshot.impl.config.remote.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.screenshot.impl.ServiceCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ServiceCaptorConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test

class ServiceCaptorConfigProtoConverterTest : CommonTest() {

    private val converter = ServiceCaptorConfigProtoConverter()

    @Test
    fun fromModel() {
        val value = ServiceCaptorConfig(
            enabled = true,
            delaySeconds = 10,
        )
        ProtoObjectPropertyAssertions(converter.fromModel(value))
            .checkField("enabled", true)
            .checkField("delaySeconds", 10L)
            .checkAll()
    }

    @Test
    fun toModel() {
        val value = ServiceCaptorConfigProto().also {
            it.enabled = true
            it.delaySeconds = 10
        }
        ObjectPropertyAssertions(converter.toModel(value))
            .checkField("enabled", true)
            .checkField("delaySeconds", 10L)
            .checkAll()
    }
}

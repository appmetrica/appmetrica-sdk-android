package io.appmetrica.analytics.screenshot.impl.config.service.converter

import io.appmetrica.analytics.screenshot.impl.ServiceCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideServiceCaptorConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.junit.Test

internal class ServiceCaptorConfigProtoConverterTest : CommonTest() {

    private val converter = ServiceCaptorConfigProtoConverter()

    @Test
    fun fromModel() {
        val value = ServiceSideServiceCaptorConfig(enabled = true, delaySeconds = 10)
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

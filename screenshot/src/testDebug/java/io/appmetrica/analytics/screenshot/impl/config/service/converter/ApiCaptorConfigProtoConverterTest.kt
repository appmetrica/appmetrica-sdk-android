package io.appmetrica.analytics.screenshot.impl.config.service.converter

import io.appmetrica.analytics.screenshot.impl.ApiCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideApiCaptorConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.junit.Test

internal class ApiCaptorConfigProtoConverterTest : CommonTest() {

    private val converter = ApiCaptorConfigProtoConverter()

    @Test
    fun fromModel() {
        val value = ServiceSideApiCaptorConfig(enabled = true)
        ProtoObjectPropertyAssertions(converter.fromModel(value))
            .checkField("enabled", true)
            .checkAll()
    }

    @Test
    fun toModel() {
        val value = ApiCaptorConfigProto().also { it.enabled = true }
        ObjectPropertyAssertions(converter.toModel(value))
            .checkField("enabled", true)
            .checkAll()
    }
}

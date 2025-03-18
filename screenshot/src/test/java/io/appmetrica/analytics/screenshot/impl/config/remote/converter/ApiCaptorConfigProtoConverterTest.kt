package io.appmetrica.analytics.screenshot.impl.config.remote.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.screenshot.impl.ApiCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ApiCaptorConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test

class ApiCaptorConfigProtoConverterTest : CommonTest() {

    private val converter = ApiCaptorConfigProtoConverter()

    @Test
    fun fromModel() {
        val value = ApiCaptorConfig(
            enabled = true,
        )
        ProtoObjectPropertyAssertions(converter.fromModel(value))
            .checkField("enabled", true)
            .checkAll()
    }

    @Test
    fun toModel() {
        val value = ApiCaptorConfigProto().also {
            it.enabled = true
        }
        ObjectPropertyAssertions(converter.toModel(value))
            .checkField("enabled", true)
            .checkAll()
    }
}

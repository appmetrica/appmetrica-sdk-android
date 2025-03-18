package io.appmetrica.analytics.screenshot.impl.config.remote.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.screenshot.impl.ContentObserverCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ContentObserverCaptorConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test

class ContentObserverCaptorConfigProtoConverterTest : CommonTest() {

    private val converter = ContentObserverCaptorConfigProtoConverter()

    @Test
    fun fromModel() {
        val value = ContentObserverCaptorConfig(
            enabled = true,
            mediaStoreColumnNames = listOf("first", "second"),
            detectWindowSeconds = 10,
        )
        ProtoObjectPropertyAssertions(converter.fromModel(value))
            .checkField("enabled", true)
            .checkField("mediaStoreColumnNames", arrayOf("first", "second"))
            .checkField("detectWindowSeconds", 10L)
            .checkAll()
    }

    @Test
    fun toModel() {
        val value = ContentObserverCaptorConfigProto().also {
            it.enabled = true
            it.mediaStoreColumnNames = arrayOf("first", "second")
            it.detectWindowSeconds = 10
        }
        ObjectPropertyAssertions(converter.toModel(value))
            .checkField("enabled", true)
            .checkField("mediaStoreColumnNames", listOf("first", "second"))
            .checkField("detectWindowSeconds", 10L)
            .checkAll()
    }
}

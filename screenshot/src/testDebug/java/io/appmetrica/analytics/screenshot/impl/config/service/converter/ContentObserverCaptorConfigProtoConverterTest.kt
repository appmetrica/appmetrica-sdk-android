package io.appmetrica.analytics.screenshot.impl.config.service.converter

import io.appmetrica.analytics.screenshot.impl.ContentObserverCaptorConfigProto
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideContentObserverCaptorConfig
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.assertions.ProtoObjectPropertyAssertions
import org.junit.Test

internal class ContentObserverCaptorConfigProtoConverterTest : CommonTest() {

    private val converter = ContentObserverCaptorConfigProtoConverter()

    @Test
    fun fromModel() {
        val value = ServiceSideContentObserverCaptorConfig(
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

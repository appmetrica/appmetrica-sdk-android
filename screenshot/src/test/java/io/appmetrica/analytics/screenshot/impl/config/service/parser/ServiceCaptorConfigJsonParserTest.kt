package io.appmetrica.analytics.screenshot.impl.config.service.parser

import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions.ObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test

internal class ServiceCaptorConfigJsonParserTest : CommonTest() {

    private val parser = ServiceCaptorConfigJsonParser()

    @Test
    fun parseIfEnabled() {
        val rawData = JSONObject()
            .put(
                "service_captor_config",
                JSONObject()
                    .put("enabled", true)
                    .put("delay_seconds", 10)
            )
        ObjectPropertyAssertions(parser.parse(rawData))
            .checkField("enabled", true)
            .checkField("delaySeconds", 10L)
            .checkAll()
    }

    @Test
    fun parseIfDisabled() {
        val rawData = JSONObject()
            .put(
                "service_captor_config",
                JSONObject()
                    .put("enabled", false)
                    .put("delay_seconds", 10)
            )
        ObjectPropertyAssertions(parser.parse(rawData))
            .checkField("enabled", false)
            .checkField("delaySeconds", 10L)
            .checkAll()
    }

    @Test
    fun parseIfNoValues() {
        val rawData = JSONObject()
            .put("service_captor_config", JSONObject())
        ObjectPropertyAssertions(parser.parse(rawData))
            .checkField("enabled", true)
            .checkField("delaySeconds", 1L)
            .checkAll()
    }

    @Test
    fun parseIfNoBlock() {
        val rawData = JSONObject()

        assertThat(parser.parse(rawData)).isNull()
    }
}

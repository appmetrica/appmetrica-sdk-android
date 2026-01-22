package io.appmetrica.analytics.screenshot.impl.config.remote.parser

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test

internal class ApiCaptorConfigJsonParserTest : CommonTest() {

    private val parser = ApiCaptorConfigJsonParser()

    @Test
    fun parseIfEnabled() {
        val rawData = JSONObject()
            .put(
                "api_captor_config",
                JSONObject()
                    .put("enabled", true)
            )
        ProtoObjectPropertyAssertions(parser.parse(rawData))
            .checkField("enabled", true)
            .checkAll()
    }

    @Test
    fun parseIfDisabled() {
        val rawData = JSONObject()
            .put(
                "api_captor_config",
                JSONObject()
                    .put("enabled", false)
            )
        ProtoObjectPropertyAssertions(parser.parse(rawData))
            .checkField("enabled", false)
            .checkAll()
    }

    @Test
    fun parseIfNoValue() {
        val rawData = JSONObject()
            .put("api_captor_config", JSONObject())
        ProtoObjectPropertyAssertions(parser.parse(rawData))
            .checkField("enabled", true)
            .checkAll()
    }

    @Test
    fun parseIfNoBlock() {
        val rawData = JSONObject()

        assertThat(parser.parse(rawData)).isNull()
    }
}

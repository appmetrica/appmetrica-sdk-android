package io.appmetrica.analytics.screenshot.impl.config.service.parser

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test

internal class ContentObserverCaptorConfigJsonParserTest : CommonTest() {

    private val parser = ContentObserverCaptorConfigJsonParser()

    @Test
    fun parseIfEnabled() {
        val rawData = JSONObject()
            .put(
                "content_observer_captor_config",
                JSONObject()
                    .put("enabled", true)
                    .put(
                        "media_store_column_names",
                        JSONArray()
                            .put("first")
                            .put("second")
                    )
                    .put("detect_window_seconds", 10)
            )
        ObjectPropertyAssertions(parser.parse(rawData))
            .checkField("enabled", true)
            .checkField("mediaStoreColumnNames", listOf("first", "second"))
            .checkField("detectWindowSeconds", 10L)
            .checkAll()
    }

    @Test
    fun parseIfDisabled() {
        val rawData = JSONObject()
            .put(
                "content_observer_captor_config",
                JSONObject()
                    .put("enabled", false)
                    .put(
                        "media_store_column_names",
                        JSONArray()
                            .put("first")
                            .put("second")
                    )
                    .put("detect_window_seconds", 10)
            )
        ObjectPropertyAssertions(parser.parse(rawData))
            .checkField("enabled", false)
            .checkField("mediaStoreColumnNames", listOf("first", "second"))
            .checkField("detectWindowSeconds", 10L)
            .checkAll()
    }

    @Test
    fun parseIfNoValues() {
        val rawData = JSONObject()
            .put("content_observer_captor_config", JSONObject())
        ObjectPropertyAssertions(parser.parse(rawData))
            .checkField("enabled", true)
            .checkField("mediaStoreColumnNames", emptyList<String>())
            .checkField("detectWindowSeconds", 5L)
            .checkAll()
    }

    @Test
    fun parseIfNoBlock() {
        val rawData = JSONObject()

        assertThat(parser.parse(rawData)).isNull()
    }
}

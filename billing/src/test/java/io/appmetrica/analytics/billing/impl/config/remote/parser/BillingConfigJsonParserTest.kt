package io.appmetrica.analytics.billing.impl.config.remote.parser

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.json.JSONObject
import org.junit.Test

class BillingConfigJsonParserTest : CommonTest() {

    private val parser = BillingConfigJsonParser()

    @Test
    fun parse() {
        val rawData = JSONObject()
            .put(
                "auto_inapp_collecting",
                JSONObject()
                    .put("send_frequency_seconds", 42)
                    .put("first_collecting_inapp_max_age_seconds", 4242)
            )

        ProtoObjectPropertyAssertions(parser.parse(rawData))
            .checkField("sendFrequencySeconds", 42)
            .checkField("firstCollectingInappMaxAgeSeconds", 4242)
            .checkAll()
    }

    @Test
    fun parseIfNoValues() {
        val rawData = JSONObject()
            .put(
                "auto_inapp_collecting",
                JSONObject()
            )

        ProtoObjectPropertyAssertions(parser.parse(rawData))
            .checkField("sendFrequencySeconds", 86400)
            .checkField("firstCollectingInappMaxAgeSeconds", 86400)
            .checkAll()
    }

    @Test
    fun parseIfNoBlock() {
        val rawData = JSONObject()

        ProtoObjectPropertyAssertions(parser.parse(rawData))
            .checkField("sendFrequencySeconds", 86400)
            .checkField("firstCollectingInappMaxAgeSeconds", 86400)
            .checkAll()
    }
}

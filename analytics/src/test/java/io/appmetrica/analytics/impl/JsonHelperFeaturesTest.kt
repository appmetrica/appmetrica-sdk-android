package io.appmetrica.analytics.impl

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.startup.FeaturesInternal
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.testutils.CommonTest
import org.json.JSONObject
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert

internal class JsonHelperFeaturesTest : CommonTest() {

    @Test
    fun featuresToJsonEmpty() {
        val input = FeaturesInternal()
        val actual = JsonHelper.featuresToJson(input)
        val expected = JSONObject()
            .put("STATUS", "UNKNOWN")
            .toString()
        JSONAssert.assertEquals(expected, actual, true)
    }

    @Test
    fun featuresToJsonFilled() {
        val input = FeaturesInternal(false, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, "some error")
        val actual = JsonHelper.featuresToJson(input)
        val expected = JSONObject()
            .put("libSslEnabled", false)
            .put("STATUS", "IDENTIFIER_PROVIDER_UNAVAILABLE")
            .put("ERROR_EXPLANATION", "some error")
            .toString()
        JSONAssert.assertEquals(expected, actual, true)
    }

    @Test
    fun featuresFromJsonNull() {
        ObjectPropertyAssertions(JsonHelper.featuresFromJson(null))
            .checkFieldIsNull("sslPinning", "getSslPinning")
            .checkField("status", "getStatus", IdentifierStatus.UNKNOWN)
            .checkFieldIsNull("errorExplanation", "getErrorExplanation")
            .checkAll()
    }

    @Test
    fun featuresFromJsonEmpty() {
        ObjectPropertyAssertions(JsonHelper.featuresFromJson(""))
            .withFinalFieldOnly(false)
            .checkFieldIsNull("sslPinning", "getSslPinning")
            .checkField("status", "getStatus", IdentifierStatus.UNKNOWN)
            .checkFieldIsNull("errorExplanation", "getErrorExplanation")
            .checkAll()
    }

    @Test
    fun featuresFromJsonInvalidJson() {
        ObjectPropertyAssertions(JsonHelper.featuresFromJson("not a json"))
            .checkFieldIsNull("sslPinning", "getSslPinning")
            .checkField("status", "getStatus", IdentifierStatus.UNKNOWN)
            .checkFieldIsNull("errorExplanation", "getErrorExplanation")
            .checkAll()
    }

    @Test
    fun featuresFromJsonEmptyJson() {
        ObjectPropertyAssertions(JsonHelper.featuresFromJson("{}"))
            .checkFieldIsNull("sslPinning", "getSslPinning")
            .checkField("status", "getStatus", IdentifierStatus.UNKNOWN)
            .checkFieldIsNull("errorExplanation", "getErrorExplanation")
            .checkAll()
    }

    @Test
    fun featuresFromJsonNFilledJson() {
        val input = JSONObject()
            .put("libSslEnabled", true)
            .put("STATUS", "IDENTIFIER_PROVIDER_UNAVAILABLE")
            .put("ERROR_EXPLANATION", "some error")
            .toString()
        ObjectPropertyAssertions(JsonHelper.featuresFromJson(input))
            .checkField("sslPinning", "getSslPinning", true)
            .checkField("status", "getStatus", IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE)
            .checkField("errorExplanation", "getErrorExplanation", "some error")
            .checkAll()
    }
}

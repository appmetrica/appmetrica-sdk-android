package io.appmetrica.analytics.impl.preloadinfo

import android.content.ContentValues
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.skyscreamer.jsonassert.JSONAssert

private const val KEY_TRACKING_ID = "tracking_id"
private const val KEY_ADDITIONAL_PARAMS = "additional_params"

@RunWith(RobolectricTestRunner::class)
internal class PreloadInfoDataParserTest : CommonTest() {

    val parser = PreloadInfoDataParser()
    private val trackingId = "222555666"
    private val additionalParams = JSONObject(mapOf("source" to "retail", "key2" to "value2"))

    @Test
    fun nullTrackingId() {
        val values = ContentValues()
        values.put(KEY_ADDITIONAL_PARAMS, additionalParams.toString())
        assertThat(parser.invoke(values)).isNull()
    }

    @Test
    fun emptyTrackingId() {
        val values = ContentValues()
        values.put(KEY_TRACKING_ID, "")
        values.put(KEY_ADDITIONAL_PARAMS, additionalParams.toString())
        assertThat(parser.invoke(values)).isNull()
    }

    @Test
    fun noParameters() {
        val values = ContentValues()
        values.put(KEY_TRACKING_ID, trackingId)
        assertThat(parser.invoke(values)).isNull()
    }

    @Test
    fun emptyStringParameters() {
        val values = ContentValues()
        values.put(KEY_TRACKING_ID, trackingId)
        values.put(KEY_ADDITIONAL_PARAMS, "")
        assertThat(parser.invoke(values)).isNull()
    }

    @Test
    fun emptyJsonParameters() {
        val values = ContentValues()
        values.put(KEY_TRACKING_ID, trackingId)
        values.put(KEY_ADDITIONAL_PARAMS, "{}")
        assertThat(parser.invoke(values)).isNull()
    }

    @Test
    fun badJsonParameters() {
        val values = ContentValues()
        values.put(KEY_TRACKING_ID, trackingId)
        values.put(KEY_ADDITIONAL_PARAMS, "not a json")
        assertThat(parser.invoke(values)).isNull()
    }

    @Test
    fun filledParameters() {
        val values = ContentValues()
        values.put(KEY_TRACKING_ID, trackingId)
        values.put(KEY_ADDITIONAL_PARAMS, additionalParams.toString())
        val result = parser.invoke(values)
        ObjectPropertyAssertions(result)
            .withIgnoredFields("additionalParameters")
            .checkField("trackingId", trackingId)
            .checkField("wasSet", true)
            .checkField("autoTrackingEnabled", false)
            .checkField("source", DistributionSource.RETAIL)
            .checkAll()
        JSONAssert.assertEquals(additionalParams, result!!.additionalParameters, true)
    }
}

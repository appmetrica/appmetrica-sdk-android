package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert

class RequestStateConverterTest : CommonTest() {

    private val requestStateKey = "request_state"
    private val typeKey = "type"
    private val lastAttemptKey = "last_attempt"
    private val prevAttemptResultKey = "prev_attempt_result"

    private val converter by setUp { RequestStateConverter() }

    @Test
    fun `fromModel for null`() {
        val result = converter.fromModel(null)
        JSONAssert.assertEquals(
            JSONObject().apply {
                put(requestStateKey, JSONArray())
            },
            JSONObject(result),
            true
        )
    }

    @Test
    fun `fromModel for empty`() {
        val result = converter.fromModel(emptyList())
        JSONAssert.assertEquals(
            JSONObject().apply {
                put(requestStateKey, JSONArray())
            },
            JSONObject(result),
            true
        )
    }

    @Test
    fun `fromModel for filled`() {
        val typeValue = "type"
        val lastAttemptValue = 100500L
        val lastAttemptResultValue = RequestAttemptResult.SUCCESS

        val result = converter.fromModel(
            listOf(
                RequestState(
                    type = typeValue,
                    lastAttempt = lastAttemptValue,
                    lastAttemptResult = lastAttemptResultValue
                )
            )
        )
        JSONAssert.assertEquals(
            JSONObject().apply {
                put(
                    requestStateKey,
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put(typeKey, typeValue)
                                put(lastAttemptKey, lastAttemptValue)
                                put(prevAttemptResultKey, lastAttemptResultValue.value)
                            }
                        )
                    }
                )
            },
            JSONObject(result),
            true
        )
    }

    @Test
    fun `toModel for null`() {
        val result = converter.toModel(null)
        assertThat(result).isEmpty()
    }

    @Test
    fun `toModel for empty string`() {
        val result = converter.toModel("")
        assertThat(result).isEmpty()
    }

    @Test
    fun `toModel for empty json`() {
        val result = converter.toModel(JSONObject().toString())
        assertThat(result).isEmpty()
    }

    @Test
    fun `toModel for empty request state`() {
        val result = converter.toModel(
            JSONObject().apply {
                put(requestStateKey, JSONArray())
            }.toString()
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `toModel for filled request state`() {
        val typeValue = "type"
        val lastAttemptValue = 100500L
        val prevAttemptResultValue = RequestAttemptResult.SUCCESS

        val result = converter.toModel(
            JSONObject().apply {
                put(
                    requestStateKey,
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put(typeKey, typeValue)
                                put(lastAttemptKey, lastAttemptValue)
                                put(prevAttemptResultKey, prevAttemptResultValue.value)
                            }
                        )
                    }
                )
            }.toString()
        )
        assertThat(result).hasSize(1)
        assertThat(result.first().type).isEqualTo(typeValue)
        assertThat(result.first().lastAttempt).isEqualTo(lastAttemptValue)
        assertThat(result.first().lastAttemptResult).isEqualTo(prevAttemptResultValue)
    }

    @Test
    fun `toModel for empty request`() {
        val result = converter.toModel(
            JSONObject().apply {
                put(
                    requestStateKey,
                    JSONArray().apply {
                        put(JSONObject())
                    }
                )
            }.toString()
        )
        assertThat(result).isEmpty()
    }
}

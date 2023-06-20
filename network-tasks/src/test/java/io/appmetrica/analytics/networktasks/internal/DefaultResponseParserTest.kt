package io.appmetrica.analytics.networktasks.internal

import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import java.util.Locale

@RunWith(ParameterizedRobolectricTestRunner::class)
class DefaultResponseParserTest(
    private val input: ByteArray?,
    expectedResponseString: String?,
    inputCaption: String?
) {

    companion object {
        private const val DEFAULT_RESPONSE_STATUS_JSON_KEY = "status"
        private const val RESPONSE_STATUS_VALID = "accepted"
        private const val RESPONSE_STATUS_INVALID = "invalid"
        private val inputValueEmptyByteArray = ByteArray(0)
        private val inputValueEmptyStringBytes = "".toByteArray()
        private val inputValueInvalidJsonBytes = "invalid json".toByteArray()
        private val inputValueEmptyJsonBytes = "{}".toByteArray()
        private const val INPUT_VALUE_WITH_JSON_WITHOUT_STATUS = "{\"custom_key\":\"custom_value\"}"
        private val inputValueWithJsonWithoutStatusBytes = INPUT_VALUE_WITH_JSON_WITHOUT_STATUS.toByteArray()
        private val inputValueWithJsonWithInvalidStatus = String.format(
            Locale.US,
            "{\"%s\":\"%s\"}",
            DEFAULT_RESPONSE_STATUS_JSON_KEY,
            RESPONSE_STATUS_INVALID
        )
        private val inputValueWithJsonWithInvalidStatusBytes = inputValueWithJsonWithInvalidStatus.toByteArray()
        private val inputValueWithJsonWithValidStatus = String.format(
            Locale.US,
            "{\"%s\":\"%s\"}",
            DEFAULT_RESPONSE_STATUS_JSON_KEY,
            RESPONSE_STATUS_VALID
        )
        private val inputValueWithJsonWithValidStatusBytes = inputValueWithJsonWithValidStatus.toByteArray()
        private val responseWithEmptyStatus = DefaultResponseParser.Response("")
        private val responseWithValidStatus = DefaultResponseParser.Response(RESPONSE_STATUS_VALID)
        private val responseWithInvalidStatus = DefaultResponseParser.Response(
            RESPONSE_STATUS_INVALID
        )
        private val parserResponseWithEmptyStatus = toJsonString(responseWithEmptyStatus)
        private val parserResponseWithValidStatus = toJsonString(responseWithValidStatus)
        private val parserResponseWithInvalidStatus = toJsonString(responseWithInvalidStatus)

        @ParameterizedRobolectricTestRunner.Parameters(name = "Input value = {2}, expected value = {1}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> {
            return listOf(
                arrayOf(null, null, "null"),
                arrayOf(inputValueEmptyByteArray, null, "byte[0]"),
                arrayOf(inputValueEmptyStringBytes, null, "\"\""),
                arrayOf(inputValueInvalidJsonBytes, null, "invalid json"),
                arrayOf(inputValueEmptyJsonBytes, parserResponseWithEmptyStatus, "{}"),
                arrayOf(
                    inputValueWithJsonWithoutStatusBytes,
                    parserResponseWithEmptyStatus,
                    INPUT_VALUE_WITH_JSON_WITHOUT_STATUS
                ),
                arrayOf(
                    inputValueWithJsonWithInvalidStatusBytes,
                    parserResponseWithInvalidStatus,
                    inputValueWithJsonWithInvalidStatus
                ),
                arrayOf(
                    inputValueWithJsonWithValidStatusBytes,
                    parserResponseWithValidStatus,
                    inputValueWithJsonWithValidStatus
                )
            )
        }

        private fun toJsonString(response: DefaultResponseParser.Response): String {
            return "{ \"$DEFAULT_RESPONSE_STATUS_JSON_KEY\" : \"${response.mStatus}\" }"
        }

        private fun fromJsonString(input: String?): DefaultResponseParser.Response? {
            return input?.let {
                with(JSONObject(it)) {
                    if (has(DEFAULT_RESPONSE_STATUS_JSON_KEY)) {
                        DefaultResponseParser.Response(getString(DEFAULT_RESPONSE_STATUS_JSON_KEY))
                    } else {
                        null
                    }
                }
            }
        }
    }

    private val expectedResponse = fromJsonString(expectedResponseString)
    private val parser =
        DefaultResponseParser()

    @Test
    fun testParseReturnExpectedResponse() {
        val parsedValue = parser.parse(input)
        if (expectedResponse == null) {
            assertThat(parsedValue).isNull()
        } else {
            assertThat(parsedValue).isEqualToComparingFieldByField(expectedResponse)
        }
    }
}

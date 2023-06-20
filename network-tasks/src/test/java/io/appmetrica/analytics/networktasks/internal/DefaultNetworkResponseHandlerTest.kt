package io.appmetrica.analytics.networktasks.internal

import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class DefaultNetworkResponseHandlerTest(
    private val responseCode: Int,
    parserResponseString: String?,
    responseHeadersString: String?,
    private val expectedResponse: DefaultResponseParser.Response?
) {

    companion object {
        private const val RESPONSE_STATUS_VALID = "accepted"
        private const val RESPONSE_STATUS_INVALID = "invalid"
        private const val DEFAULT_RESPONSE_STATUS_JSON_KEY = "status"
        private const val EMPTY_HEADERS = "{}"
        private const val CUSTOM_HEADERS = "{\"header_key\":\"header_value\"}"

        @ParameterizedRobolectricTestRunner.Parameters(
            name = "Return result = {3} for responseCode = {0}, " +
                "parserResponse = {1}, responseHeaders = {2}"
        )
        @JvmStatic
        fun data(): Collection<Array<Any?>> {
            val responseWithEmptyStatus = DefaultResponseParser.Response("")
            val responseWithValidStatus = DefaultResponseParser.Response(RESPONSE_STATUS_VALID)
            val responseWithInvalidStatus = DefaultResponseParser.Response(RESPONSE_STATUS_INVALID)
            val parserResponseWithEmptyStatus = toJsonString(responseWithEmptyStatus)
            val parserResponseWithValidStatus = toJsonString(responseWithValidStatus)
            val parserResponseWithInvalidStatus = toJsonString(responseWithInvalidStatus)
            return listOf(
                arrayOf(200, parserResponseWithValidStatus, null, responseWithValidStatus),
                arrayOf(200, parserResponseWithValidStatus, EMPTY_HEADERS, responseWithValidStatus),
                arrayOf(200, parserResponseWithValidStatus, CUSTOM_HEADERS, responseWithValidStatus),
                arrayOf(100, null, null, null),
                arrayOf(100, parserResponseWithValidStatus, null, null),
                arrayOf(100, parserResponseWithValidStatus, EMPTY_HEADERS, null),
                arrayOf(100, parserResponseWithValidStatus, CUSTOM_HEADERS, null),
                arrayOf(200, null, null, null), arrayOf<Any?>(200, null, EMPTY_HEADERS, null),
                arrayOf(200, null, CUSTOM_HEADERS, null),
                arrayOf(200, parserResponseWithEmptyStatus, null, responseWithEmptyStatus),
                arrayOf(200, parserResponseWithEmptyStatus, EMPTY_HEADERS, responseWithEmptyStatus),
                arrayOf(200, parserResponseWithEmptyStatus, CUSTOM_HEADERS, responseWithEmptyStatus),
                arrayOf(200, parserResponseWithInvalidStatus, null, responseWithInvalidStatus),
                arrayOf(200, parserResponseWithInvalidStatus, EMPTY_HEADERS, responseWithInvalidStatus),
                arrayOf(200, parserResponseWithInvalidStatus, CUSTOM_HEADERS, responseWithInvalidStatus)
            )
        }

        @JvmStatic
        private fun toJsonString(response: DefaultResponseParser.Response): String {
            return "{\"$DEFAULT_RESPONSE_STATUS_JSON_KEY\" : \"${response.mStatus}\"}"
        }

        @JvmStatic
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

    private val responseHeaders = responseHeadersString?.let { JSONObject(it).toMap() }
    private val parserResponse = fromJsonString(parserResponseString)
    private val parser = mock<DefaultResponseParser> {
        on { this.parse(any()) } doReturn parserResponse
    }
    private val handler =
        DefaultNetworkResponseHandler(parser)
    private val responseDataHolder = mock<ResponseDataHolder> {
        on { this.responseCode } doReturn responseCode
        on { this.responseHeaders } doReturn responseHeaders
        on { this.responseData } doReturn ByteArray(1024)
    }

    @Test
    fun testHandleReturnExpectedResult() {
        val result = handler.handle(responseDataHolder)
        if (expectedResponse == null) {
            assertThat(result).isNull()
        } else {
            assertThat(result).isEqualToComparingFieldByField(expectedResponse)
        }
    }

    private fun JSONObject.toMap(): Map<String, List<String>?> {
        return keys().asSequence().associate {
            it as String to optJSONArray(it)?.toStringList()
        }
    }

    private fun JSONArray.toStringList(): List<String> {
        return (0 until length()).map { getString(it) }
    }
}

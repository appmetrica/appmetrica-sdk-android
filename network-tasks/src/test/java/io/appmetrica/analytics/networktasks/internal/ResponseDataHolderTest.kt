package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing

class ResponseDataHolderTest : CommonTest() {

    private val validityChecker = mock<ResponseValidityChecker>()
    private val responseDataHolder =
        ResponseDataHolder(validityChecker)

    @Test
    fun responseCodeDefault() {
        assertThat(responseDataHolder.responseCode).isZero
    }

    @Test
    fun responseDataDefault() {
        assertThat(responseDataHolder.responseData).isNull()
    }

    @Test
    fun responseHeadersDefault() {
        assertThat(responseDataHolder.responseHeaders).isNull()
    }

    @Test
    fun setResponseCode() {
        val code = 485768
        responseDataHolder.responseCode = code
        assertThat(responseDataHolder.responseCode).isEqualTo(code)
    }

    @Test
    fun setResponseData() {
        val responseData = "data".toByteArray()
        responseDataHolder.responseData = responseData
        assertThat(responseDataHolder.responseData).isEqualTo(responseData)
    }

    @Test
    fun setResponseHeaders() {
        val headers = mapOf("key1" to listOf("header1", "header2"), "key2" to listOf("header3"))
        responseDataHolder.responseHeaders = headers
        assertThat(responseDataHolder.responseHeaders).isEqualTo(headers)
    }

    @Test
    fun isValidResponseTrue() {
        val code = 999
        stubbing(validityChecker) {
            on { this.isResponseValid(code) } doReturn true
        }
        responseDataHolder.responseCode = code
        assertThat(responseDataHolder.isValidResponse).isTrue
    }

    @Test
    fun isValidResponseFalse() {
        val code = 999
        stubbing(validityChecker) {
            on { this.isResponseValid(code) } doReturn false
        }
        responseDataHolder.responseCode = code
        assertThat(responseDataHolder.isValidResponse).isFalse
    }
}

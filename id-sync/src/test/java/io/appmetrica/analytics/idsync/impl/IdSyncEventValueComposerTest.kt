package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.skyscreamer.jsonassert.JSONAssert

internal class IdSyncEventValueComposerTest : CommonTest() {

    private val typeKey = "type"
    private val urlKey = "url"
    private val responseCodeKey = "responseCode"
    private val responseBodyKey = "responseBody"
    private val responseHeadersKey = "responseHeaders"

    private val type = "type"
    private val url = "url"
    private val responseCode = 200
    private val responseBody = "responseBody".toByteArray()
    private val responseHeaders = mapOf("responseHeaderKey" to listOf("responseHeaderValue"))

    val requestResult: RequestResult = mock {
        on { type } doReturn type
        on { url } doReturn url
        on { responseCode } doReturn responseCode
        on { responseBody } doReturn responseBody
        on { responseHeaders } doReturn responseHeaders
    }

    private val composer by setUp { IdSyncEventValueComposer() }

    @Test
    fun `compose for filled`() {
        val result = composer.compose(requestResult)
        val expected = JSONObject().apply {
            put(typeKey, type)
            put(urlKey, url)
            put(responseCodeKey, responseCode)
            put(responseBodyKey, String(responseBody))
            put(responseHeadersKey, JSONObject(responseHeaders))
        }
        JSONAssert.assertEquals(expected, JSONObject(result), true)
    }

    @Test
    fun `compose for empty`() {
        whenever(requestResult.type).thenReturn("")
        whenever(requestResult.url).thenReturn(null)
        whenever(requestResult.responseCode).thenReturn(0)
        whenever(requestResult.responseBody).thenReturn(byteArrayOf())
        whenever(requestResult.responseHeaders).thenReturn(mapOf())

        val result = composer.compose(requestResult)
        val expected = JSONObject().apply {
            put(typeKey, "")
            put(urlKey, null)
            put(responseCodeKey, 0)
            put(responseBodyKey, "")
            put(responseHeadersKey, JSONObject())
        }
        JSONAssert.assertEquals(expected, JSONObject(result), true)
    }

    @Test
    fun `compose for invalid UTF8 string`() {
        whenever(requestResult.responseBody).thenReturn(
            byteArrayOf(
                0x80.toByte(),
                0xC0.toByte(),
                0x41.toByte()
            )
        )

        val result = composer.compose(requestResult)
        assertThat(JSONObject(result).getString(responseBodyKey)).isNotEmpty
    }

    @Test
    fun `compose for broken surrogate pair`() {
        whenever(requestResult.responseBody).thenReturn(
            ("\uD800" + "A").toByteArray(Charsets.UTF_8)
        )

        val result = composer.compose(requestResult)
        assertThat(JSONObject(result).getString(responseBodyKey)).isNotEmpty
    }
}

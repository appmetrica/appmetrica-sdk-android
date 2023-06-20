package io.appmetrica.analytics.network.internal

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.AbstractMap
import kotlin.random.Random

class ResponseTest {

    private val completed = Random.nextBoolean()
    private val code = 666
    private val responseData = "aaa".toByteArray()
    private val errorData = "bbb".toByteArray()
    private val headers = mapOf("key1" to listOf("header1", "header2"), "key2" to listOf("header3", "header4"))
    private val exception = RuntimeException()

    @Test
    fun createFilledObject() {
        val response = Response(completed, code, responseData, errorData, headers, exception)
        ObjectPropertyAssertions(response)
            .withPrivateFields(true)
            .checkField("completed", "isCompleted", completed)
            .checkField("code", "getCode", code)
            .checkField("responseData", "getResponseData", responseData)
            .checkField("errorData", "getErrorData", errorData)
            .checkField("headers", "getHeaders", headers)
            .checkField("exception", "getException", exception)
            .checkAll()
    }

    @Test
    fun doesNotSeeHeaderUpdates() {
        val mutableHeaders = mutableMapOf("key1" to listOf("header1"))
        val response = Response(completed, code, responseData, errorData, mutableHeaders, exception)
        mutableHeaders["key2"] = listOf("header2")
        assertThat(response.headers).containsExactly(AbstractMap.SimpleEntry("key1", listOf("header1")))
    }

    @Test(expected = UnsupportedOperationException::class)
    fun cannotUpdateHeaders() {
        val response = Response(completed, code, responseData, errorData, headers, exception)
        response.headers["key11"] = listOf()
    }

    @Test
    fun createEmptyObject() {
        val response = Response(false, 0, ByteArray(0), ByteArray(0), null, null)
        ObjectPropertyAssertions(response)
            .withPrivateFields(true)
            .checkField("completed", "isCompleted", false)
            .checkField("code", "getCode", 0)
            .checkField("responseData", "getResponseData", ByteArray(0))
            .checkField("errorData", "getErrorData", ByteArray(0))
            .checkField("headers", "getHeaders", emptyMap<String, List<String>>())
            .checkFieldIsNull("exception", "getException")
            .checkAll()
    }

    @Test
    fun createObjectOnlyWithThrowable() {
        val response = Response(exception)
        ObjectPropertyAssertions(response)
            .withPrivateFields(true)
            .checkField("completed", "isCompleted", false)
            .checkField("code", "getCode", 0)
            .checkField("responseData", "getResponseData", ByteArray(0))
            .checkField("errorData", "getErrorData", ByteArray(0))
            .checkField("headers", "getHeaders", emptyMap<String, List<String>>())
            .checkField("exception", "getException", exception)
            .checkAll()
    }
}

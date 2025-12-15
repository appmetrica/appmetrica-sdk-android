package io.appmetrica.analytics.networkapi

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import java.io.IOException

class ResponseTest : CommonTest() {

    @Test
    fun builderWithSuccessResponse() {
        val responseData = "response body".toByteArray()
        val headers = mapOf("Content-Type" to listOf("application/json"))

        val response = Response.Builder(
            isCompleted = true,
            code = 200,
            responseData = responseData
        )
            .withHeaders(headers)
            .withUrl("https://example.com")
            .build()

        ObjectPropertyAssertions(response)
            .checkField("isCompleted", true)
            .checkField("code", 200)
            .checkField("responseData", responseData)
            .checkField("headers", headers)
            .checkFieldIsNull("exception")
            .checkField("url", "https://example.com")
            .checkAll()
    }

    @Test
    fun builderWithErrorResponse() {
        val errorData = "error body".toByteArray()

        val response = Response.Builder(
            isCompleted = true,
            code = 404,
            responseData = errorData
        )
            .withUrl("https://example.com")
            .build()

        ObjectPropertyAssertions(response)
            .checkField("isCompleted", true)
            .checkField("code", 404)
            .checkField("responseData", errorData)
            .checkField("headers", emptyMap<String, List<String>>())
            .checkFieldIsNull("exception")
            .checkField("url", "https://example.com")
            .checkAll()
    }

    @Test
    fun builderWithException() {
        val exception = IOException("Network timeout")

        val response = Response.Builder(exception)
            .withUrl("https://example.com")
            .build()

        SoftAssertions().apply {
            assertThat(response.isCompleted).`as`("isCompleted").isFalse()
            assertThat(response.code).`as`("code").isEqualTo(0)
            assertThat(response.responseData).`as`("responseData").isEmpty()
            assertThat(response.exception).`as`("exception").isSameAs(exception)
            assertThat(response.url).`as`("url").isEqualTo("https://example.com")
            assertAll()
        }
    }

    @Test
    fun builderWithMinimalData() {
        val response = Response.Builder(
            isCompleted = false,
            code = 404,
            responseData = ByteArray(0)
        ).build()

        ObjectPropertyAssertions(response)
            .checkField("isCompleted", false)
            .checkField("code", 404)
            .checkField("responseData", ByteArray(0))
            .checkField("headers", emptyMap<String, List<String>>())
            .checkFieldIsNull("exception")
            .checkFieldIsNull("url")
            .checkAll()
    }

    @Test
    fun builderWithHeadersCreatesNewMap() {
        val originalHeaders = mutableMapOf("Content-Type" to listOf("text/plain"))
        val response = Response.Builder(
            isCompleted = true,
            code = 200,
            responseData = ByteArray(0)
        )
            .withHeaders(originalHeaders)
            .build()

        originalHeaders["X-Custom"] = listOf("value")

        SoftAssertions().apply {
            assertThat(response.headers).`as`("does not contain X-Custom").doesNotContainKey("X-Custom")
            assertThat(response.headers).`as`("headers size").hasSize(1)
            assertAll()
        }
    }

    @Test
    fun toStringWithSuccessResponse() {
        val response = Response.Builder(
            isCompleted = true,
            code = 200,
            responseData = "test".toByteArray()
        )
            .withUrl("https://example.com")
            .build()

        val string = response.toString()

        SoftAssertions().apply {
            assertThat(string).`as`("contains isCompleted=true").contains("isCompleted=true")
            assertThat(string).`as`("contains code=200").contains("code=200")
            assertThat(string).`as`("contains responseDataLength=4").contains("responseDataLength=4")
            assertThat(string).`as`("contains url").contains("url=https://example.com")
            assertAll()
        }
    }

    @Test
    fun toStringWithException() {
        val exception = IOException("Network error")
        val response = Response.Builder(exception).build()

        val string = response.toString()

        SoftAssertions().apply {
            assertThat(string).`as`("contains isCompleted=false").contains("isCompleted=false")
            assertThat(string).`as`("contains exception").contains("exception=java.io.IOException: Network error")
            assertAll()
        }
    }

    @Test
    fun builderWithMultipleHeaders() {
        val headers = mapOf(
            "Content-Type" to listOf("application/json", "charset=utf-8"),
            "X-Custom" to listOf("value1", "value2"),
            "Cache-Control" to listOf("no-cache")
        )

        val response = Response.Builder(
            isCompleted = true,
            code = 200,
            responseData = ByteArray(0)
        )
            .withHeaders(headers)
            .build()

        SoftAssertions().apply {
            assertThat(response.headers).`as`("headers size").hasSize(3)
            assertThat(response.headers["Content-Type"]).`as`("Content-Type")
                .containsExactly("application/json", "charset=utf-8")
            assertThat(response.headers["X-Custom"]).`as`("X-Custom").containsExactly("value1", "value2")
            assertThat(response.headers["Cache-Control"]).`as`("Cache-Control").containsExactly("no-cache")
            assertAll()
        }
    }
}

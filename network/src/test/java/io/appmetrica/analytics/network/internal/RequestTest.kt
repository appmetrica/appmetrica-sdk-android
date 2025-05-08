package io.appmetrica.analytics.network.internal

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RequestTest : CommonTest() {

    @Test
    fun createFilledObject() {
        val url = "some url"
        val method = "some method"
        val actual = Request.Builder(url)
            .withMethod(method)
            .addHeader("key1", "value1")
            .addHeader("key2", "value2")
            .build()
        ObjectPropertyAssertions(actual)
            .withPrivateFields(true)
            .checkField("url", "getUrl", url)
            .checkField("method", "getMethod", method)
            .checkField("body", "getBody", ByteArray(0))
            .checkField("headers", "getHeaders", mapOf("key1" to "value1", "key2" to "value2"))
            .checkAll()
    }

    @Test
    fun createPostObject() {
        val url = "some url"
        val body = "some body".toByteArray()
        val actual = Request.Builder(url)
            .post(body)
            .addHeader("key1", "value1")
            .addHeader("key2", "value2")
            .build()
        ObjectPropertyAssertions(actual)
            .withPrivateFields(true)
            .checkField("url", "getUrl", url)
            .checkField("method", "getMethod", "POST")
            .checkField("body", "getBody", body)
            .checkField("headers", "getHeaders", mapOf("key1" to "value1", "key2" to "value2"))
            .checkAll()
    }

    @Test
    fun createEmptyObject() {
        val url = "some url"
        val actual = Request.Builder(url).build()
        ObjectPropertyAssertions(actual)
            .withPrivateFields(true)
            .checkField("url", "getUrl", url)
            .checkField("method", "getMethod", "GET")
            .checkField("body", "getBody", ByteArray(0))
            .checkField("headers", "getHeaders", emptyMap<String, String>())
            .checkAll()
    }

    @Test
    fun emptyMethod() {
        val url = "some url"
        val actual = Request.Builder(url).withMethod("").build()
        assertThat(actual.method).isEqualTo("GET")
    }

    @Test(expected = UnsupportedOperationException::class)
    fun cannotUpdateHeaders() {
        val actual = Request.Builder("some url").build()
        actual.headers["key1"] = "value1"
    }
}

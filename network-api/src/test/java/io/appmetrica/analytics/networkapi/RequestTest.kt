package io.appmetrica.analytics.networkapi

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test

class RequestTest : CommonTest() {

    @Test
    fun builderWithOnlyUrl() {
        val request = Request.Builder("https://example.com").build()

        ObjectPropertyAssertions(request)
            .checkField("url", "https://example.com")
            .checkField("method", Request.Method.GET)
            .checkField("body", ByteArray(0))
            .checkField("headers", emptyMap<String, String>())
            .checkAll()
    }

    @Test
    fun builderWithHeaders() {
        val request = Request.Builder("https://example.com")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer token123")
            .addHeader("Accept", "application/json")
            .build()

        SoftAssertions().apply {
            assertThat(request.headers).`as`("headers size").hasSize(3)
            assertThat(request.headers["Content-Type"]).`as`("Content-Type").isEqualTo("application/json")
            assertThat(request.headers["Authorization"]).`as`("Authorization").isEqualTo("Bearer token123")
            assertThat(request.headers["Accept"]).`as`("Accept").isEqualTo("application/json")
            assertAll()
        }
    }

    @Test
    fun builderWithPost() {
        val body = "request body".toByteArray()
        val request = Request.Builder("https://example.com")
            .withMethod(Request.Method.POST)
            .withBody(body)
            .build()

        SoftAssertions().apply {
            assertThat(request.method).`as`("method").isEqualTo(Request.Method.POST)
            assertThat(request.body).`as`("body").isEqualTo(body)
            assertAll()
        }
    }

    @Test
    fun builderWithBodyOnly() {
        val body = "body without method change".toByteArray()
        val request = Request.Builder("https://example.com")
            .withBody(body)
            .build()

        SoftAssertions().apply {
            assertThat(request.method).`as`("method").isEqualTo(Request.Method.GET)
            assertThat(request.body).`as`("body").isEqualTo(body)
            assertAll()
        }
    }

    @Test
    fun builderMethodCanBeOverridden() {
        val body = "body".toByteArray()
        val request = Request.Builder("https://example.com")
            .withMethod(Request.Method.POST)
            .withBody(body)
            .withMethod(Request.Method.GET)
            .build()

        SoftAssertions().apply {
            assertThat(request.method).`as`("method").isEqualTo(Request.Method.GET)
            assertThat(request.body).`as`("body").isEqualTo(body)
            assertAll()
        }
    }

    @Test
    fun builderHeadersAreCopied() {
        val builder = Request.Builder("https://example.com")
            .addHeader("X-Header-1", "value1")

        val request1 = builder.build()
        builder.addHeader("X-Header-2", "value2")
        val request2 = builder.build()

        SoftAssertions().apply {
            assertThat(request1.headers).`as`("request1 headers size").hasSize(1)
            assertThat(request1.headers).`as`("request1 contains X-Header-1").containsKey("X-Header-1")
            assertThat(request1.headers).`as`("request1 does not contain X-Header-2").doesNotContainKey("X-Header-2")
            assertThat(request2.headers).`as`("request2 headers size").hasSize(2)
            assertThat(request2.headers).`as`("request2 contains both headers").containsKeys("X-Header-1", "X-Header-2")
            assertAll()
        }
    }

    @Test
    fun builderWithComplexUrl() {
        val url = "https://api.example.com:8080/v1/users?id=123&filter=active#section"
        val request = Request.Builder(url).build()

        assertThat(request.url).isEqualTo(url)
    }

    @Test
    fun builderWithHttpUrl() {
        val request = Request.Builder("http://example.com").build()

        assertThat(request.url).isEqualTo("http://example.com")
    }

    @Test
    fun builderReplaceHeaderValue() {
        val request = Request.Builder("https://example.com")
            .addHeader("X-Custom", "value1")
            .addHeader("X-Custom", "value2")
            .build()

        assertThat(request.headers["X-Custom"]).isEqualTo("value2")
    }

    @Test
    fun builderFullChain() {
        val body = "{\"key\":\"value\"}".toByteArray()
        val request = Request.Builder("https://api.example.com/endpoint")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer token")
            .withMethod(Request.Method.POST)
            .withBody(body)
            .build()

        ObjectPropertyAssertions(request)
            .checkField("url", "https://api.example.com/endpoint")
            .checkField("method", Request.Method.POST)
            .checkField("body", body)
            .checkField("headers", mapOf("Content-Type" to "application/json", "Authorization" to "Bearer token"))
            .checkAll()
    }
}

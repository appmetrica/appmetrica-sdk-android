package io.appmetrica.analytics.networklegacy.impl

import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test

internal class CallImplTest : CommonTest() {

    private val settings = NetworkClientSettings.Builder()
        .withConnectTimeout(5000)
        .withReadTimeout(10000)
        .withMaxResponseSize(1024 * 1024)
        .build()

    @Test
    fun `execute with non-https url returns error response`() {
        val request = Request.Builder("http://example.com").build()
        val call = CallImpl(settings, request)

        val response = call.execute()

        SoftAssertions().apply {
            assertThat(response.isCompleted).`as`("isCompleted").isFalse()
            assertThat(response.exception).`as`("exception").isInstanceOf(IllegalArgumentException::class.java)
            assertThat(response.exception?.message).`as`("exception message")
                .contains("does not represent https connection")
            assertAll()
        }
    }

    @Test
    fun `execute with invalid url returns error response`() {
        val request = Request.Builder("invalid://url").build()
        val call = CallImpl(settings, request)

        val response = call.execute()

        SoftAssertions().apply {
            assertThat(response.isCompleted).`as`("isCompleted").isFalse()
            assertThat(response.code).`as`("code").isEqualTo(0)
            assertThat(response.responseData).`as`("responseData").isEmpty()
            assertThat(response.exception).`as`("exception").isNotNull()
            assertAll()
        }
    }

    @Test
    fun `execute with post request includes body`() {
        val body = "test body".toByteArray()
        val request = Request.Builder("https://httpbin.org/post")
            .withMethod(Request.Method.POST)
            .withBody(body)
            .build()
        val call = CallImpl(settings, request)

        val response = call.execute()

        assertThat(response).isNotNull()
    }

    @Test
    fun `multiple execute calls work independently`() {
        val request1 = Request.Builder("https://www.google.com").build()
        val request2 = Request.Builder("http://example.com").build()

        val call1 = CallImpl(settings, request1)
        val call2 = CallImpl(settings, request2)

        val response1 = call1.execute()
        val response2 = call2.execute()

        SoftAssertions().apply {
            assertThat(response1).`as`("response1").isNotNull()
            assertThat(response2).`as`("response2").isNotNull()
            assertThat(response2.isCompleted).`as`("response2 isCompleted").isFalse()
            assertThat(response2.exception).`as`("response2 exception")
                .isInstanceOf(IllegalArgumentException::class.java)
            assertAll()
        }
    }
}

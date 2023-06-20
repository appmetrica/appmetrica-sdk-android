package io.appmetrica.analytics.network.impl

import io.appmetrica.analytics.network.internal.Request
import io.appmetrica.analytics.network.internal.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.lang.reflect.Modifier

class ToStringSpecialCasesTest {

    @Test
    fun requestToString() {
        val request = Request.Builder("some url").build()
        val extractedFieldAndValues = ToStringTestUtils.extractFieldsAndValues(
            Request::class.java,
            request,
            Modifier.PRIVATE or Modifier.FINAL,
            setOf("sslSocketFactory", "body")
        )
        ToStringTestUtils.testToString(request, extractedFieldAndValues)
    }

    @Test
    fun requestToStringContainsBodyLength() {
        val body = "aaaaabbbbb".toByteArray()
        val request = Request.Builder("some url").post(body).build()
        assertThat(request.toString()).contains("bodyLength=10")
    }

    @Test
    fun responseToString() {
        val response = Response(true, 123, "aaa".toByteArray(), "bbb".toByteArray(), emptyMap(), RuntimeException())
        val extractedFieldAndValues = ToStringTestUtils.extractFieldsAndValues(
            Response::class.java,
            response,
            Modifier.PRIVATE or Modifier.FINAL,
            setOf("responseData", "errorData")
        )
        ToStringTestUtils.testToString(response, extractedFieldAndValues)
    }

    @Test
    fun responseToStringContainsDataLengths() {
        val response = Response(true, 123, "aaa".toByteArray(), "bbbb".toByteArray(), emptyMap(), RuntimeException())
        assertThat(response.toString()).contains("responseDataLength=3", "errorDataLength=4")
    }
}

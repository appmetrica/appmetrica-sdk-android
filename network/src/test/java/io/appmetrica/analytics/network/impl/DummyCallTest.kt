package io.appmetrica.analytics.network.impl

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.SoftAssertions
import org.junit.Test

internal class DummyCallTest : CommonTest() {

    @Test
    fun `execute returns response with exception`() {
        val call = DummyCall()

        val response = call.execute()

        SoftAssertions().also {
            it.assertThat(response).isNotNull()
            it.assertThat(response.isCompleted).isFalse()
            it.assertThat(response.exception).isInstanceOf(IllegalStateException::class.java)
            it.assertThat(response.exception?.message).isEqualTo("This is dummy call")
            it.assertThat(response.code).isEqualTo(0)
            it.assertThat(response.responseData).isEmpty()
            it.assertThat(response.headers).isEmpty()
            it.assertAll()
        }
    }
}

package io.appmetrica.analytics.networktasks.internal

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import javax.net.ssl.HttpsURLConnection

internal class DefaultResponseValidityCheckerTest : CommonTest() {

    private val responseValidityChecker = DefaultResponseValidityChecker()

    @Test
    fun isResponseValidForOk() {
        assertThat(responseValidityChecker.isResponseValid(HttpsURLConnection.HTTP_OK)).isTrue()
    }

    @Test
    fun isResponseValidForBadRequest() {
        assertThat(responseValidityChecker.isResponseValid(HttpsURLConnection.HTTP_BAD_REQUEST))
            .isFalse()
    }

    @Test
    fun isResponseValidForInternalError() {
        assertThat(responseValidityChecker.isResponseValid(HttpsURLConnection.HTTP_INTERNAL_ERROR))
            .isFalse()
    }

}

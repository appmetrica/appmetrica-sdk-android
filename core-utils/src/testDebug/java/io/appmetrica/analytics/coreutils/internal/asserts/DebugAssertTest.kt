package io.appmetrica.analytics.coreutils.internal.asserts

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DebugAssertTest : CommonTest() {

    @Test
    fun assertNotNullNotNull() {
        DebugAssert.assertNotNull(Any(), "message")
    }

    @Test
    fun assertNotNullNull() {
        val errorMessage = "error message"
        val thrown = assertThrows(AssertionError::class.java) {
            DebugAssert.assertNotNull(null, errorMessage)
        }
        assertThat(thrown.message).isEqualTo(errorMessage)
    }
}

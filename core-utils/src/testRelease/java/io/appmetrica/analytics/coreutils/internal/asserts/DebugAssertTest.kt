package io.appmetrica.analytics.coreutils.internal.asserts

import io.appmetrica.analytics.testutils.CommonTest
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
        DebugAssert.assertNotNull(null, "message")
    }
}

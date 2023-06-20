package io.appmetrica.analytics.coreutils.internal.asserts

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DebugAssertTest {

    @Test
    fun assertNotNullNotNull() {
        DebugAssert.assertNotNull(Any(), "message")
    }

    @Test
    fun assertNotNullNull() {
        DebugAssert.assertNotNull(null, "message")
    }
}

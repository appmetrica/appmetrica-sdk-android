package io.appmetrica.analytics.coreutils.internal.asserts

import io.appmetrica.gradle.testutils.CommonTest
import org.junit.Test

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

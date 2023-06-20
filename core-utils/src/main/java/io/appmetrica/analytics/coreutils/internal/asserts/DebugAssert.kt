package io.appmetrica.analytics.coreutils.internal.asserts

import io.appmetrica.analytics.coreutils.DebugProvider

object DebugAssert {

    @JvmStatic
    fun assertNotNull(value: Any?, errorMessage: String) {
        if (DebugProvider.DEBUG) {
            if (value == null) {
                throw AssertionError(errorMessage)
            }
        }
    }
}

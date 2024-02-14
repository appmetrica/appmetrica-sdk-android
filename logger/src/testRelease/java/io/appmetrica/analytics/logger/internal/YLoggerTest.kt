package io.appmetrica.analytics.logger.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class YLoggerTest {

    @Test
    fun debugFlagValue() {
        assertThat(YLogger.DEBUG).isFalse()
    }
}

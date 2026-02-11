package io.appmetrica.analytics.impl.proxy.validation

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class ConfigCheckerTest : CommonTest() {

    private var configChecker = ConfigChecker("")

    @Test
    fun validMaxReportsInDatabaseCount() {
        assertThat(configChecker.getCheckedMaxReportsInDatabaseCount(500))
            .isEqualTo(500)
    }

    @Test
    fun maxReportsInDatabaseCountTooSmall() {
        assertThat(configChecker.getCheckedMaxReportsInDatabaseCount(50))
            .isEqualTo(100)
    }

    @Test
    fun maxReportsInDatabaseCountTooBig() {
        assertThat(configChecker.getCheckedMaxReportsInDatabaseCount(20000))
            .isEqualTo(10000)
    }
}

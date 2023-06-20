package io.appmetrica.analytics.impl.proxy.validation

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConfigCheckerTest : CommonTest() {

    private var configChecker = ConfigChecker("")

    @Test
    fun testValidMaxReportsInDatabaseCount() {
        assertThat(configChecker.getCheckedMaxReportsInDatabaseCount(500))
            .isEqualTo(500)
    }

    @Test
    fun testMaxReportsInDatabaseCountTooSmall() {
        assertThat(configChecker.getCheckedMaxReportsInDatabaseCount(50))
            .isEqualTo(100)
    }

    @Test
    fun testMaxReportsInDatabaseCountTooBig() {
        assertThat(configChecker.getCheckedMaxReportsInDatabaseCount(20000))
            .isEqualTo(10000)
    }
}

package io.appmetrica.analytics.impl

import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.Test
import kotlin.time.Duration.Companion.days

internal class SavedAppMetricaConfigTtlCheckerTest : CommonTest() {

    private val savedAt = 1_000_000L
    private val ttl = 30.days.inWholeMilliseconds

    @Test
    fun isExpired() {
        assertSoftly {
            it.assertThat(SavedAppMetricaConfigTtlChecker.isExpired(savedAt, savedAt - 1))
                .describedAs("clock skew: now before savedAt")
                .isFalse()
            it.assertThat(SavedAppMetricaConfigTtlChecker.isExpired(savedAt, savedAt))
                .describedAs("exactly at savedAt")
                .isFalse()
            it.assertThat(SavedAppMetricaConfigTtlChecker.isExpired(savedAt, savedAt + ttl - 1))
                .describedAs("one ms before ttl")
                .isFalse()
            it.assertThat(SavedAppMetricaConfigTtlChecker.isExpired(savedAt, savedAt + ttl))
                .describedAs("exactly at ttl")
                .isTrue()
            it.assertThat(SavedAppMetricaConfigTtlChecker.isExpired(savedAt, savedAt + ttl + 1))
                .describedAs("one ms after ttl")
                .isTrue()
            it.assertAll()
        }
    }
}

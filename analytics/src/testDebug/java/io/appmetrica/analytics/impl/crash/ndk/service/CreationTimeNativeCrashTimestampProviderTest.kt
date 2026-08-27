package io.appmetrica.analytics.impl.crash.ndk.service

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrash
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class CreationTimeNativeCrashTimestampProviderTest : CommonTest() {

    private val currentTime = 1700000000000L
    private val creationTime = 1699000000000L

    @get:Rule
    val systemTimeProviderConstructionRule = constructionRule<SystemTimeProvider> {
        on { currentTimeMillis() } doReturn currentTime
    }

    private val provider: CreationTimeNativeCrashTimestampProvider by setUp {
        CreationTimeNativeCrashTimestampProvider()
    }

    @Test
    fun `getTimestamp when creationTime is positive`() {
        val crash: AppMetricaNativeCrash = mock { on { creationTime } doReturn creationTime }
        assertThat(provider.getTimestamp(crash)).isEqualTo(creationTime)
    }

    @Test
    fun `getTimestamp when creationTime is zero`() {
        val crash: AppMetricaNativeCrash = mock { on { creationTime } doReturn 0L }
        assertThat(provider.getTimestamp(crash)).isEqualTo(currentTime)
    }

    @Test
    fun `getTimestamp when creationTime is negative`() {
        val crash: AppMetricaNativeCrash = mock { on { creationTime } doReturn -1L }
        assertThat(provider.getTimestamp(crash)).isEqualTo(currentTime)
    }
}

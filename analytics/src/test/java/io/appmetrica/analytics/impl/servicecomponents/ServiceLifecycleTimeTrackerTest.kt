package io.appmetrica.analytics.impl.servicecomponents

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

class ServiceLifecycleTimeTrackerTest : CommonTest() {

    private val initialTimestamp = 100500L

    @get:Rule
    val systemTimeProviderMockedConstructionRule = constructionRule<SystemTimeProvider> {
        on { currentTimeMillis() } doReturn initialTimestamp
    }
    private val systemTimeProvider by systemTimeProviderMockedConstructionRule

    private val serviceSystemTimeTracker: ServiceLifecycleTimeTracker by setUp { ServiceLifecycleTimeTracker() }

    @Test
    fun offsetInSecondsSinceCreation() {
        val deltaSeconds = 10L
        whenever(systemTimeProvider.currentTimeMillis())
            .thenReturn(initialTimestamp + TimeUnit.SECONDS.toMillis(deltaSeconds))
        assertThat(serviceSystemTimeTracker.offsetInSecondsSinceCreation(TimeUnit.SECONDS)).isEqualTo(deltaSeconds)
    }
}

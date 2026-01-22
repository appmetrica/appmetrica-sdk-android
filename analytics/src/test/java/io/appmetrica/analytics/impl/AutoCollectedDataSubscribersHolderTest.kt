package io.appmetrica.analytics.impl

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

internal class AutoCollectedDataSubscribersHolderTest : CommonTest() {

    private val componentId: ComponentId = mock()
    private val preferencesComponentDbStorage: PreferencesComponentDbStorage = mock()
    private val mapCaptor = argumentCaptor<Map<String, Long>>()

    private val initialTime = 100500L
    private val updatedTime = 200500L

    @get:Rule
    val timeProviderRule = constructionRule<SystemTimeProvider> {
        on { currentTimeMillis() }.thenReturn(initialTime)
    }
    private val timeProvider by timeProviderRule

    private val firstObserver = "first"
    private val secondObserver = "second"
    private val thirdObserver = "third"
    private val initialObservers = setOf(firstObserver, secondObserver)
    private val updatedObservers = setOf(secondObserver, thirdObserver)

    private val holder by setUp { AutoCollectedDataSubscribersHolder(componentId, preferencesComponentDbStorage) }

    @Test
    fun updateSubscribers() {
        holder.updateSubscribers(initialObservers)
        assertThat(holder.getSubscribers()).containsExactlyInAnyOrder(firstObserver, secondObserver)
        verify(preferencesComponentDbStorage).putAutoCollectedDataSubscribers(mapCaptor.capture())
        verify(preferencesComponentDbStorage).putAutoCollectedDataSubscribers(mapCaptor.capture())
        assertThat(mapCaptor.firstValue)
            .containsAllEntriesOf(initialObservers.associateWith { initialTime }.toMap())

        clearInvocations(preferencesComponentDbStorage)
        whenever(timeProvider.currentTimeMillis()).thenReturn(updatedTime)

        holder.updateSubscribers(updatedObservers)
        assertThat(holder.getSubscribers()).containsExactlyInAnyOrder(firstObserver, secondObserver, thirdObserver)
        verify(preferencesComponentDbStorage).putAutoCollectedDataSubscribers(mapCaptor.capture())
        assertThat(mapCaptor.lastValue)
            .containsAllEntriesOf(
                mapOf(
                    firstObserver to initialTime,
                    secondObserver to updatedTime,
                    thirdObserver to updatedTime
                )
            )
    }

    @Test
    fun observersExpiration() {
        holder.updateSubscribers(initialObservers)
        whenever(timeProvider.currentTimeMillis()).thenReturn(initialTime + TimeUnit.DAYS.toMillis(6))
        holder.updateSubscribers(updatedObservers)
        whenever(timeProvider.currentTimeMillis()).thenReturn(initialTime + TimeUnit.DAYS.toMillis(7) + 1)

        clearInvocations(preferencesComponentDbStorage)

        holder.updateSubscribers(setOf("One new"))

        assertThat(holder.getSubscribers()).doesNotContain(firstObserver)
        assertThat(holder.getSubscribers()).contains(secondObserver, thirdObserver)

        verify(preferencesComponentDbStorage).putAutoCollectedDataSubscribers(mapCaptor.capture())
        assertThat(mapCaptor.lastValue)
            .containsAllEntriesOf(
                mapOf(
                    secondObserver to initialTime + TimeUnit.DAYS.toMillis(6),
                    thirdObserver to initialTime + TimeUnit.DAYS.toMillis(6),
                )
            ).doesNotContainKeys(firstObserver)
    }

    @Test
    fun ttlUpdateInterval() {
        holder.updateSubscribers(initialObservers)
        whenever(timeProvider.currentTimeMillis()).thenReturn(initialTime + TimeUnit.SECONDS.toMillis(50))

        clearInvocations(preferencesComponentDbStorage)
        holder.updateSubscribers(initialObservers)

        verifyNoInteractions(preferencesComponentDbStorage)

        whenever(timeProvider.currentTimeMillis()).thenReturn(initialTime + TimeUnit.SECONDS.toMillis(70))
        holder.updateSubscribers(initialObservers)

        verify(preferencesComponentDbStorage).putAutoCollectedDataSubscribers(mapCaptor.capture())
        assertThat(mapCaptor.lastValue)
            .containsAllEntriesOf(
                mapOf(
                    firstObserver to initialTime + TimeUnit.SECONDS.toMillis(70),
                    secondObserver to initialTime + TimeUnit.SECONDS.toMillis(70)
                )
            )
    }
}

package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PendingReportsCountHolderTest : CommonTest() {

    private val initialCount = 5L

    private val databaseHelper: DatabaseHelper = mock {
        on { eventsCount } doReturn initialCount
    }

    private val pendingReportsCountHolder: PendingReportsCountHolder by setUp {
        PendingReportsCountHolder(databaseHelper)
    }

    @Test
    fun pendingReportsCount() {
        assertThat(pendingReportsCountHolder.pendingReportsCount).isEqualTo(initialCount)
    }

    @Test
    fun `pendingReportsCount after event added`() {
        val addedEventsCount = 10
        val addedEvents = List(addedEventsCount) { 5 }
        pendingReportsCountHolder.onEventsAdded(addedEvents)
        assertThat(pendingReportsCountHolder.pendingReportsCount).isEqualTo(initialCount + addedEventsCount)
    }

    @Test
    fun `pendingReportsCount after event removed`() {
        val removedEventsCount = 4
        val removedEvents = List(removedEventsCount) { 2 }
        pendingReportsCountHolder.onEventsRemoved(removedEvents)
        assertThat(pendingReportsCountHolder.pendingReportsCount).isEqualTo(initialCount - removedEventsCount)
    }

    @Test
    fun `pendingReportsCount after event updated`() {
        val newEventsCountFromDb = 100500L
        whenever(databaseHelper.eventsCount).thenReturn(newEventsCountFromDb)
        pendingReportsCountHolder.onEventsUpdated()
        assertThat(pendingReportsCountHolder.pendingReportsCount).isEqualTo(newEventsCountFromDb)
    }
}

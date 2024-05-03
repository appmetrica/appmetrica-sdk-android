package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MaxReportsCountReachedConditionTest : CommonTest() {

    private val databaseHelper: DatabaseHelper = mock()

    private val config: ReportRequestConfig = mock {
        on { maxReportsCount } doReturn 2
    }

    private val configHolder: ReportComponentConfigurationHolder = mock {
        on { get() } doReturn config
    }

    private val type = 0
    private val maxReportsCountReachedCondition: MaxReportsCountReachedCondition by setUp {
        MaxReportsCountReachedCondition(databaseHelper, configHolder)
    }

    @Test
    fun listenerAdded() {
        verify(databaseHelper).addEventListener(maxReportsCountReachedCondition)
    }

    @Test
    fun reportsCountGreaterThanMax() {
        maxReportsCountReachedCondition.onEventsAdded(listOf(type, type, type))
        assertThat(maxReportsCountReachedCondition.isConditionMet).isTrue()
    }

    @Test
    fun reportsCountEqualToMax() {
        maxReportsCountReachedCondition.onEventsAdded(listOf(type, type))
        assertThat(maxReportsCountReachedCondition.isConditionMet).isTrue()
    }

    @Test
    fun reportsCountLessThanMax() {
        maxReportsCountReachedCondition.onEventsAdded(listOf(type))
        assertThat(maxReportsCountReachedCondition.isConditionMet).isFalse()
    }

    @Test
    fun freshConfigIsApplied() {
        val config: ReportRequestConfig = mock {
            on { maxReportsCount } doReturn 3
        }
        whenever(configHolder.get()).thenReturn(config)
        maxReportsCountReachedCondition.onEventsAdded(listOf(type, type))
        assertThat(maxReportsCountReachedCondition.isConditionMet).isFalse()
    }

    @Test
    fun stateIsReadFromDb() {
        whenever(databaseHelper.eventsCount).thenReturn(2L)
        whenever(config.maxReportsCount).thenReturn(2)

        assertThat(MaxReportsCountReachedCondition(databaseHelper, configHolder).isConditionMet).isTrue()
    }

    @Test
    fun eventsRemoved() {
        maxReportsCountReachedCondition.onEventsAdded(listOf(type, type, type))
        assertThat(maxReportsCountReachedCondition.isConditionMet).isTrue()
        maxReportsCountReachedCondition.onEventsRemoved(listOf(type, type))
        assertThat(maxReportsCountReachedCondition.isConditionMet).isFalse()
    }

    @Test
    fun onEventsUpdated() {
        maxReportsCountReachedCondition.onEventsAdded(listOf(type, type, type))
        maxReportsCountReachedCondition.onEventsUpdated()
        assertThat(maxReportsCountReachedCondition.isConditionMet).isFalse()
    }
}

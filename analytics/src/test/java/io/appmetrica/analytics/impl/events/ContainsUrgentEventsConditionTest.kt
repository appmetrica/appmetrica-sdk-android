package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ContainsUrgentEventsConditionTest : CommonTest() {

    private val urgentEvents: Set<Int> = setOf(
        InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION.typeId,
        InternalEvents.EVENT_TYPE_APP_UPDATE.typeId,
        InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.typeId,
        InternalEvents.EVENT_TYPE_INIT.typeId,
        InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT.typeId,
        InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT.typeId,
        InternalEvents.EVENT_TYPE_SEND_REFERRER.typeId,
        InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT.typeId,
    )

    private val databaseHelper: DatabaseHelper = mock()

    private val urgentEventsCondition: ContainsUrgentEventsCondition by setUp {
        ContainsUrgentEventsCondition(databaseHelper)
    }

    @Test
    fun urgentReportSavedThenRemoved() {
        val type = InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.typeId
        urgentEventsCondition.onEventsAdded(listOf(type))
        urgentEventsCondition.onEventsRemoved(listOf(type))
        assertThat(urgentEventsCondition.isConditionMet).isFalse()
    }

    @Test
    fun twoUrgentReportsSavedOneRemoved() {
        val type = InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.typeId
        urgentEventsCondition.onEventsAdded(listOf(type, type))
        urgentEventsCondition.onEventsRemoved(listOf(type))
        assertThat(urgentEventsCondition.isConditionMet).isTrue()
    }

    @Test
    fun noReportsSaved() {
        assertThat(urgentEventsCondition.isConditionMet).isFalse()
    }

    @Test
    fun urgentEventSavedNotUrgentRemoved() {
        val firstType = InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.typeId
        val customType = InternalEvents.EVENT_TYPE_CUSTOM_EVENT.typeId
        urgentEventsCondition.onEventsAdded(listOf(firstType))
        urgentEventsCondition.onEventsRemoved(listOf(customType))
        assertThat(urgentEventsCondition.isConditionMet).isTrue()
    }

    @Test
    fun stateIsReadFromDb() {
        whenever(databaseHelper.getEventsOfFollowingTypesCount(urgentEvents)).thenReturn(1L)
        val condition = ContainsUrgentEventsCondition(databaseHelper)
        assertThat(condition.isConditionMet).isTrue()
    }

    @Test
    fun onEventsUpdated() {
        whenever(databaseHelper.getEventsOfFollowingTypesCount(urgentEvents)).thenReturn(1L)
        val condition = ContainsUrgentEventsCondition(databaseHelper)
        condition.onEventsRemoved(listOf(InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.typeId))
        assertThat(condition.isConditionMet).isFalse()
        condition.onEventsUpdated()
        assertThat(condition.isConditionMet).isTrue()
    }
}

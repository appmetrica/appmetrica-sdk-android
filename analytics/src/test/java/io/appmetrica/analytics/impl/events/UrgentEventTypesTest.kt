package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.db.DatabaseHelper
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class UrgentEventTypesTest(
    private val eventType: Int,
    private val isUrgent: Boolean
) : CommonTest() {

    companion object {
        private val urgentEvents: Set<Int> = setOf(
            InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.typeId,
            InternalEvents.EVENT_TYPE_APP_UPDATE.typeId,
            InternalEvents.EVENT_TYPE_INIT.typeId,
            InternalEvents.EVENT_TYPE_SEND_REFERRER.typeId
        )

        @ParameterizedRobolectricTestRunner.Parameters(name = "Type {0} urgent? {1}")
        @JvmStatic
        fun data(): Collection<Array<Any>> =
            InternalEvents.values().map { arrayOf(it.typeId, urgentEvents.contains(it.typeId)) }
    }

    private val databaseHelper: DatabaseHelper = mock()

    private val urgentEventsCondition: ContainsUrgentEventsCondition by setUp {
        ContainsUrgentEventsCondition(databaseHelper)
    }

    @Test
    fun isConditionMet() {
        urgentEventsCondition.onEventsAdded(listOf(eventType))
        assertThat(urgentEventsCondition.isConditionMet).isEqualTo(isUrgent)
    }
}

package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.db.DatabaseHelper
import java.util.concurrent.atomic.AtomicLong

internal class ContainsUrgentEventsCondition(
    private val databaseHelper: DatabaseHelper
) : EventCondition, EventListener {

    private val urgentEventsCount =
        AtomicLong(databaseHelper.getEventsOfFollowingTypesCount(UrgentEvents.urgentEventTypes))

    init {
        databaseHelper.addEventListener(this)
    }

    override fun isConditionMet(): Boolean = urgentEventsCount.get() > 0

    override fun onEventsAdded(reportTypes: List<Int>) {
        val newUrgentEventsNumber = reportTypes.count { it in UrgentEvents.urgentEventTypes }
        urgentEventsCount.addAndGet(newUrgentEventsNumber.toLong())
    }

    override fun onEventsRemoved(reportTypes: List<Int>) {
        val deletedUrgentEventsNumber = reportTypes.count { it in UrgentEvents.urgentEventTypes }
        urgentEventsCount.addAndGet(-deletedUrgentEventsNumber.toLong())
    }

    override fun onEventsUpdated() {
        val newValue = databaseHelper.getEventsOfFollowingTypesCount(UrgentEvents.urgentEventTypes)
        urgentEventsCount.set(newValue)
    }
}

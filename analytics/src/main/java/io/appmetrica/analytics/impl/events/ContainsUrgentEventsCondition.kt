package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.db.DatabaseHelper
import java.util.concurrent.atomic.AtomicLong

class ContainsUrgentEventsCondition(
    private val databaseHelper: DatabaseHelper
) : EventCondition, EventListener {

    private val urgentEvents = setOf(
        InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION.typeId,
        InternalEvents.EVENT_TYPE_APP_UPDATE.typeId,
        InternalEvents.EVENT_TYPE_FIRST_ACTIVATION.typeId,
        InternalEvents.EVENT_TYPE_INIT.typeId,
        InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT.typeId,
        InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT.typeId,
        InternalEvents.EVENT_TYPE_SEND_REFERRER.typeId,
        InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT.typeId,
    )

    private val urgentEventsCount = AtomicLong(databaseHelper.getEventsOfFollowingTypesCount(urgentEvents))

    init {
        databaseHelper.addEventListener(this)
    }

    override fun isConditionMet(): Boolean = urgentEventsCount.get() > 0

    override fun onEventsAdded(reportTypes: List<Int>) {
        val newUrgentEventsNumber = reportTypes.count { it in urgentEvents }
        urgentEventsCount.addAndGet(newUrgentEventsNumber.toLong())
    }

    override fun onEventsRemoved(reportTypes: List<Int>) {
        val deletedUrgentEventsNumber = reportTypes.count { it in urgentEvents }
        urgentEventsCount.addAndGet(-deletedUrgentEventsNumber.toLong())
    }

    override fun onEventsUpdated() {
        val newValue = databaseHelper.getEventsOfFollowingTypesCount(urgentEvents)
        urgentEventsCount.set(newValue)
    }
}

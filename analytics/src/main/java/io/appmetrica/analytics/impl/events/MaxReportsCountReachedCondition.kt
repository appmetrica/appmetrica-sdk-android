package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.DatabaseHelper
import java.util.concurrent.atomic.AtomicLong

class MaxReportsCountReachedCondition(
    private val databaseHelper: DatabaseHelper,
    private val configHolder: ReportComponentConfigurationHolder
) : EventCondition, EventListener {

    private val reportsCount: AtomicLong = AtomicLong(databaseHelper.eventsCount)

    init {
        databaseHelper.addEventListener(this)
    }

    override fun isConditionMet(): Boolean = reportsCount.get() >= configHolder.get().maxReportsCount

    override fun onEventsAdded(reportTypes: List<Int>) {
        reportsCount.addAndGet(reportTypes.size.toLong())
    }

    override fun onEventsRemoved(reportTypes: List<Int>) {
        reportsCount.addAndGet(-reportTypes.size.toLong())
    }

    override fun onEventsUpdated() {
        reportsCount.set(databaseHelper.eventsCount)
    }
}

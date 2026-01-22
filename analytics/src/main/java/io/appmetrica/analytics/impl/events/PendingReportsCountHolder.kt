package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.db.DatabaseHelper
import java.util.concurrent.atomic.AtomicLong

internal class PendingReportsCountHolder(
    private val databaseHelper: DatabaseHelper
) : EventListener, PendingReportsCountProvider {

    private val reportsCount = AtomicLong(databaseHelper.eventsCount)

    init {
        databaseHelper.addEventListener(this)
    }

    override fun onEventsAdded(reportTypes: List<Int>) {
        reportsCount.addAndGet(reportTypes.size.toLong())
    }

    override fun onEventsRemoved(reportTypes: List<Int>) {
        reportsCount.addAndGet(-reportTypes.size.toLong())
    }

    override fun onEventsUpdated() {
        reportsCount.set(databaseHelper.eventsCount)
    }

    override val pendingReportsCount: Long
        get() = reportsCount.get()
}

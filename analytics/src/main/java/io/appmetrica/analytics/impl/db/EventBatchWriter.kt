package io.appmetrica.analytics.impl.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventModelConverter
import io.appmetrica.analytics.impl.events.EventListener
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.utils.PublicLogConstructor
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

/**
 * Handles batch writing of events to the database and notifying listeners.
 * This class encapsulates the logic of persisting events and managing event counts.
 */
internal class EventBatchWriter(
    private val storage: DatabaseStorage,
    private val component: ComponentUnit,
    private val rowCount: AtomicLong,
    private val eventListeners: List<EventListener>,
    private val databaseCleaner: DatabaseCleaner,
    lock: ReentrantReadWriteLock
) {

    private val tag = "[EventBatchWriter]"
    private val writeLock = lock.writeLock()

    /**
     * Writes a batch of events to the database.
     * This method is thread-safe and handles database overflow by deleting excessive events.
     *
     * @param reports List of events to write
     */
    fun writeEvents(reports: List<ContentValues>) {
        if (reports.isEmpty()) return

        val dbLimit = component.freshReportRequestConfig.maxEventsInDbCount
        var wDatabase: SQLiteDatabase? = null
        writeLock.withLock {
            try {
                wDatabase = storage.writableDatabase

                wDatabase?.let { db ->
                    db.beginTransaction()

                    for (reportItem in reports) {
                        db.insertOrThrow(Constants.EventsTable.TABLE_NAME, null, reportItem)
                        rowCount.incrementAndGet()
                        logEvent(reportItem)
                    }

                    val rows = rowCount.get()
                    DebugLogger.info(tag, "report saved. Row count $rows; dbLimit: $dbLimit;")

                    var deletedRowsCount = 0
                    if (rows > dbLimit) {
                        deletedRowsCount = deleteExcessiveReports(db)
                        val actualEventsCount = rowCount.addAndGet(-deletedRowsCount.toLong())
                        DebugLogger.info(
                            tag,
                            "Reports table cleared. $deletedRowsCount rows deleted. Row count: $actualEventsCount"
                        )
                    }

                    db.setTransactionSuccessful()

                    if (deletedRowsCount != 0) {
                        for (listener in eventListeners) {
                            listener.onEventsUpdated()
                        }
                    }
                }
            } catch (exception: Throwable) {
                DebugLogger.error(tag, exception, "Smth was wrong while inserting reports into database.")
            } finally {
                Utils.endTransaction(wDatabase)
            }
        }
    }

    /**
     * Notifies all registered listeners about added events and triggers event processing.
     *
     * @param reports List of events that were added
     */
    fun notifyListeners(reports: List<ContentValues>) {
        val reportTypes = reports.map { getReportType(it) }
        for (listener in eventListeners) {
            listener.onEventsAdded(reportTypes)
        }
        component.eventTrigger.trigger()
    }

    private fun deleteExcessiveReports(db: SQLiteDatabase): Int {
        return try {
            val percentToDelete = 10
            val whereClause = String.format(
                Constants.EventsTable.DELETE_EXCESSIVE_RECORDS_WHERE,
                EventsManager.EVENTS_WITH_FIRST_HIGHEST_PRIORITY.joinToString(", "),
                EventsManager.EVENTS_WITH_SECOND_HIGHEST_PRIORITY.joinToString(", "),
                percentToDelete
            )
            databaseCleaner.cleanEvents(
                db,
                Constants.EventsTable.TABLE_NAME,
                whereClause,
                null,
                DatabaseCleaner.Reason.DB_OVERFLOW,
                component.componentId.apiKey,
                true
            ).mDeletedRowsCount
        } catch (e: Throwable) {
            DebugLogger.error(tag, e, "Something was wrong while removing excessive reports from db")
            AppMetricaSelfReportFacade.getReporter().reportError("deleteExcessiveReports exception", e)
            0
        }
    }

    private fun logEvent(reportItem: ContentValues) {
        if (EventsManager.isPublicForLogs(getReportType(reportItem))) {
            val dbEventModel = DbEventModelConverter().toModel(reportItem)
            component.publicLogger.info(
                PublicLogConstructor.constructLogValueForInternalEvent(
                    "Event saved to db",
                    dbEventModel.type,
                    dbEventModel.description.name,
                    dbEventModel.description.value
                )
            )
        }
    }

    private fun getReportType(reportItem: ContentValues): Int =
        reportItem.getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE) ?: -1
}

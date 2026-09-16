package io.appmetrica.analytics.impl.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.event.DbEventModel
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventModelConverter
import io.appmetrica.analytics.impl.events.EventListener
import io.appmetrica.analytics.impl.events.EventTrigger
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.utils.PublicLogConstructor
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.on
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.thread

internal class EventBatchWriterTest : CommonTest() {

    private val dbLimit = 100L
    private val apiKey = "test-api-key"
    private val threshold = 12345L

    private val database: SQLiteDatabase = mock()
    private val storage: DatabaseStorage = mock {
        on { writableDatabase } doReturn database
    }
    private val reportRequestConfig: ReportRequestConfig = mock {
        on { maxEventsInDbCount } doReturn dbLimit
    }
    private val componentId: ComponentId = mock {
        on { apiKey } doReturn apiKey
    }
    private val eventTrigger: EventTrigger = mock()
    private val publicLogger: PublicLogger = mock()
    private val sessionManager: SessionManagerStateMachine = mock {
        on { thresholdSessionIdForActualSessions } doReturn threshold
    }
    private val component: ComponentUnit = mock {
        on { freshReportRequestConfig } doReturn reportRequestConfig
        on { componentId } doReturn componentId
        on { eventTrigger } doReturn eventTrigger
        on { publicLogger } doReturn publicLogger
        on { sessionManager } doReturn sessionManager
    }
    private val rowCount = AtomicLong(0)
    private val listener: EventListener = mock()
    private val databaseCleaner: DatabaseCleaner = mock()
    private val lock = ReentrantReadWriteLock()

    private val dbEventModelDescription: DbEventModel.Description = mock()
    private val dbEventModel: DbEventModel = mock {
        on { description } doReturn dbEventModelDescription
    }

    @get:Rule
    val eventsManagerRule = staticRule<EventsManager> {
        on { EventsManager.isPublicForLogs(any<Int>()) } doReturn false
    }

    @get:Rule
    val publicLogConstructorRule = staticRule<PublicLogConstructor>()

    @get:Rule
    val dbEventModelConverterRule = constructionRule<DbEventModelConverter> {
        on { toModel(any()) } doReturn dbEventModel
    }

    private lateinit var writer: EventBatchWriter

    @Before
    fun setUp() {
        writer = EventBatchWriter(
            storage = storage,
            component = component,
            rowCount = rowCount,
            eventListeners = listOf(listener),
            databaseCleaner = databaseCleaner,
            lock = lock
        )
    }

    @Test
    fun `writeEvents does nothing when list is empty`() {
        writer.writeEvents(emptyList())

        inOrder(storage) {
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `writeEvents inserts all events and commits transaction`() {
        writer.writeEvents(listOf(ContentValues(), ContentValues(), ContentValues()))

        inOrder(database) {
            verify(database).beginTransaction()
            verify(database, times(3)).insertOrThrow(eq(Constants.EventsTable.TABLE_NAME), isNull(), any())
            verify(database).setTransactionSuccessful()
        }
    }

    @Test
    fun `writeEvents increments rowCount for each event`() {
        rowCount.set(0)

        writer.writeEvents(listOf(ContentValues(), ContentValues(), ContentValues()))

        assertThat(rowCount.get()).isEqualTo(3)
    }

    @Test
    fun `writeEvents does not delete when row count is at or below limit`() {
        rowCount.set(0)

        writer.writeEvents(listOf(ContentValues()))

        verifyNoInteractions(databaseCleaner)
        verify(listener, never()).onEventsUpdated()
    }

    @Test
    fun `overflow deletes events updates rowCount notifies listeners and requests cleanup`() {
        rowCount.set(100)
        whenever(reportRequestConfig.maxEventsInDbCount).thenReturn(100L)
        whenever(databaseCleaner.cleanEvents(any(), any(), any(), anyOrNull(), any(), any(), any()))
            .thenReturn(DatabaseCleaner.DeletionInfo(null, 10))

        val cleanupNeeded = writer.writeEvents(listOf(ContentValues()))

        verify(databaseCleaner).cleanEvents(
            eq(database), eq(Constants.EventsTable.TABLE_NAME), any(), isNull(),
            eq(DatabaseCleaner.Reason.DB_OVERFLOW), eq(apiKey), eq(true)
        )
        assertThat(rowCount.get()).isEqualTo(91)
        verify(listener).onEventsUpdated()
        assertThat(cleanupNeeded).isTrue()
    }

    @Test
    fun `writeEvents does not request cleanup when no events were deleted`() {
        rowCount.set(100)
        whenever(reportRequestConfig.maxEventsInDbCount).thenReturn(100L)
        whenever(databaseCleaner.cleanEvents(any(), any(), any(), anyOrNull(), any(), any(), any()))
            .thenReturn(DatabaseCleaner.DeletionInfo(null, 0))

        val cleanupNeeded = writer.writeEvents(listOf(ContentValues()))

        assertThat(cleanupNeeded).isFalse()
        assertThat(rowCount.get()).isEqualTo(101)
        verify(listener, never()).onEventsUpdated()
        verify(database, never()).delete(
            eq(Constants.SessionTable.TABLE_NAME),
            eq(Constants.SessionTable.CLEAR_EMPTY_PREVIOUS_SESSIONS),
            any()
        )
    }

    @Test
    fun `writeEvents does not request cleanup when overflow cleanup fails`() {
        rowCount.set(100)
        whenever(reportRequestConfig.maxEventsInDbCount).thenReturn(100L)
        whenever(databaseCleaner.cleanEvents(any(), any(), any(), anyOrNull(), any(), any(), any()))
            .thenThrow(RuntimeException("cleanup failed"))

        val cleanupNeeded = writer.writeEvents(listOf(ContentValues()))

        assertThat(cleanupNeeded).isFalse()
        assertThat(rowCount.get()).isEqualTo(101)
        verify(listener, never()).onEventsUpdated()
    }

    @Test
    fun `empty sessions cleanup uses fresh threshold while holding session monitor and database lock`() {
        whenever(database.delete(any(), any(), any())).thenAnswer {
            assertThat(Thread.holdsLock(sessionManager)).isTrue()
            assertThat(lock.isWriteLockedByCurrentThread).isTrue()
            0
        }
        rowCount.set(100)
        whenever(reportRequestConfig.maxEventsInDbCount).thenReturn(100L)
        whenever(databaseCleaner.cleanEvents(any(), any(), any(), anyOrNull(), any(), any(), any()))
            .thenReturn(DatabaseCleaner.DeletionInfo(null, 10))

        assertThat(writer.writeEvents(listOf(ContentValues()))).isTrue()
        verify(sessionManager, never()).thresholdSessionIdForActualSessions
        val freshThreshold = threshold - 1
        whenever(sessionManager.thresholdSessionIdForActualSessions).thenReturn(freshThreshold)

        writer.deleteEmptyOverflowedSessions()

        verify(database).delete(
            eq(Constants.SessionTable.TABLE_NAME),
            eq(Constants.SessionTable.CLEAR_EMPTY_PREVIOUS_SESSIONS),
            eq(arrayOf(freshThreshold.toString()))
        )
    }

    @Test
    fun `overflow event transaction finishes while session manager is busy`() {
        val realSessionManager = SessionManagerStateMachine(component, mock(), mock(), mock(), mock())
        whenever(component.sessionManager).thenReturn(realSessionManager)
        rowCount.set(dbLimit)
        val overflowReached = CountDownLatch(1)
        val writeFinished = CountDownLatch(1)
        whenever(databaseCleaner.cleanEvents(any(), any(), any(), anyOrNull(), any(), any(), any()))
            .thenAnswer {
                overflowReached.countDown()
                DatabaseCleaner.DeletionInfo(null, 10)
            }
        val report: ContentValues = mock()
        val writerThread = thread(start = false, name = "overflow-contention-test") {
            try {
                writer.writeEvents(listOf(report))
            } finally {
                writeFinished.countDown()
            }
        }

        val finishedWithoutSessionMonitor: Boolean
        try {
            finishedWithoutSessionMonitor = synchronized(realSessionManager) {
                writerThread.start()
                assertThat(overflowReached.await(5, TimeUnit.SECONDS)).isTrue()
                writeFinished.await(5, TimeUnit.SECONDS).also { finished ->
                    if (!finished) {
                        println(writerThread.stackTrace.joinToString("\n"))
                    }
                }
            }
        } finally {
            writerThread.join(TimeUnit.SECONDS.toMillis(5))
        }

        assertThat(writerThread.isAlive).isFalse()
        assertThat(finishedWithoutSessionMonitor)
            .describedAs("Event transaction must not wait for the session manager monitor")
            .isTrue()
        verify(database).setTransactionSuccessful()
        verify(database).endTransaction()
        assertThat(lock.isWriteLocked).isFalse()
    }

    @Test
    fun `writeEvents does not delete sessions when no overflow`() {
        rowCount.set(0)

        writer.writeEvents(listOf(ContentValues()))

        verify(database, never()).delete(
            eq(Constants.SessionTable.TABLE_NAME),
            any(),
            any()
        )
    }

    @Test
    fun `writeEvents catches exception without throwing`() {
        whenever(database.insertOrThrow(any(), isNull(), any())).doThrow(RuntimeException("DB error"))

        writer.writeEvents(listOf(ContentValues()))
    }

    @Test
    fun `writeEvents does not call publicLogger for non-public event type`() {
        writer.writeEvents(listOf(ContentValues()))

        verify(publicLogger, never()).info(anyOrNull())
    }

    @Test
    fun `writeEvents calls publicLogger for public event type`() {
        val logMessage = "Event saved to db: EVENT_TYPE_REGULAR"
        whenever(EventsManager.isPublicForLogs(any<Int>())).thenReturn(true)
        whenever(PublicLogConstructor.constructLogValueForInternalEvent(any(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(logMessage)

        writer.writeEvents(listOf(ContentValues()))

        verify(publicLogger).info(logMessage)
    }

    @Test
    fun `notifyListeners calls onEventsAdded with event types and triggers`() {
        val event1 = mock<ContentValues> {
            on { getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE) } doReturn 1
        }
        val event2 = mock<ContentValues> {
            on { getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE) } doReturn 2
        }

        writer.notifyListeners(listOf(event1, event2))

        inOrder(listener, eventTrigger) {
            verify(listener).onEventsAdded(listOf(1, 2))
            verify(eventTrigger).trigger()
        }
    }
}

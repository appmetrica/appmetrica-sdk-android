package io.appmetrica.analytics.impl.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.component.ComponentUnit
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
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantReadWriteLock

internal class EventBatchWriterTest : CommonTest() {

    private val dbLimit = 100L
    private val apiKey = "test-api-key"

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
    private val component: ComponentUnit = mock {
        on { freshReportRequestConfig } doReturn reportRequestConfig
        on { componentId } doReturn componentId
        on { eventTrigger } doReturn eventTrigger
        on { publicLogger } doReturn publicLogger
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
    fun `writeEvents deletes excessive events adjusts rowCount and notifies listeners when over limit`() {
        rowCount.set(100)
        whenever(reportRequestConfig.maxEventsInDbCount).thenReturn(100L)
        whenever(databaseCleaner.cleanEvents(any(), any(), any(), anyOrNull(), any(), any(), any()))
            .thenReturn(DatabaseCleaner.DeletionInfo(null, 10))

        writer.writeEvents(listOf(ContentValues()))

        verify(databaseCleaner).cleanEvents(
            eq(database), eq(Constants.EventsTable.TABLE_NAME), any(), isNull(),
            eq(DatabaseCleaner.Reason.DB_OVERFLOW), eq(apiKey), eq(true)
        )
        assertThat(rowCount.get()).isEqualTo(91)
        verify(listener).onEventsUpdated()
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

package io.appmetrica.analytics.impl.db.storage

import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.coreutils.internal.io.CloseableUtils.closeSafely
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.db.connectors.DBConnector
import io.appmetrica.analytics.impl.db.constants.TempCacheTable
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.LogRule
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TempCacheDbHelperTest : CommonTest() {

    private val tableName = "test_table"

    private val idToDelete = 35L
    private val intervalToDelete = 100000L
    private val deletedRecords = 16

    private val scope = "scope#1"
    private val timestamp = 200500L
    private val data = ByteArray(10) { it.toByte() }
    private val secondTimestamp = 300500L
    private val secondData = ByteArray(12) { it.toByte() }
    private val firstId = 1L
    private val secondId = 2L
    private val limit = 20

    private val dataCursor = MatrixCursor(
        arrayOf(
            TempCacheTable.Column.ID,
            TempCacheTable.Column.SCOPE,
            TempCacheTable.Column.TIMESTAMP,
            TempCacheTable.Column.DATA
        )
    ).apply {
        addRow(arrayOf(firstId, scope, timestamp, data))
        addRow(arrayOf(secondId, scope, secondTimestamp, secondData))
    }

    private val database: SQLiteDatabase = mock {
        on {
            query(
                eq(false),
                eq(tableName),
                eq(null),
                any(),
                any(),
                eq(null),
                eq(null),
                any(),
                any()
            )
        } doReturn dataCursor
        on { delete(eq(tableName), any(), any()) } doReturn deletedRecords
    }

    private val emptyCursor = mock<Cursor> {
        on { moveToNext() } doReturn false
    }

    private val dbConnector: DBConnector = mock {
        on { openDb() } doReturn database
    }

    private val contentValuesCaptor = argumentCaptor<ContentValues>()
    private val stringCaptor = argumentCaptor<String>()
    private val stringArrayCaptor = argumentCaptor<Array<String>>()

    private val now = 300500L

    @get:Rule
    val timeProviderMockedConstructionRule = constructionRule<SystemTimeProvider> {
        on { currentTimeMillis() } doReturn now
    }

    private val timeProvider: SystemTimeProvider by timeProviderMockedConstructionRule

    @get:Rule
    val logRule = LogRule()

    private val dbHelper by setUp { TempCacheDbHelper(dbConnector, tableName) }

    @Test
    fun put() {
        val id = 100L
        whenever(database.insertOrThrow(eq(tableName), eq(null), any()))
            .thenReturn(id)

        assertThat(dbHelper.put(scope, timestamp, data)).isEqualTo(id)
        verify(database).insertOrThrow(eq(tableName), eq(null), contentValuesCaptor.capture())
        verify(database).closeSafely()

        assertThat(contentValuesCaptor.allValues).hasSize(1)
        val savedValue = contentValuesCaptor.firstValue
        assertThat(savedValue.keySet())
            .containsExactlyInAnyOrder(
                TempCacheTable.Column.SCOPE,
                TempCacheTable.Column.DATA,
                TempCacheTable.Column.TIMESTAMP
            )
        assertThat(savedValue[TempCacheTable.Column.SCOPE]).isEqualTo(scope)
        assertThat(savedValue[TempCacheTable.Column.DATA]).isEqualTo(data)
        assertThat(savedValue[TempCacheTable.Column.TIMESTAMP]).isEqualTo(timestamp)
    }

    @Test
    fun `put if no db`() {
        whenever(dbConnector.openDb()).thenReturn(null)
        assertThat(dbHelper.put(scope, timestamp, data)).isEqualTo(-1)
    }

    @Test
    fun `put if openDb throws exception`() {
        whenever(dbConnector.openDb()).thenThrow(RuntimeException())
        assertThat(dbHelper.put(scope, timestamp, data)).isEqualTo(-1)
    }

    @Test
    fun `put if insertOrThrow throws exception`() {
        whenever(database.insertOrThrow(eq(tableName), eq(null), any())).thenThrow(RuntimeException())
        assertThat(dbHelper.put(scope, timestamp, data)).isEqualTo(-1)
        verify(database).closeSafely()
    }

    @Test
    fun `get multiple entries`() {
        assertThat(dbHelper.get(scope, limit))
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                TempCacheEntry(firstId, scope, timestamp, data),
                TempCacheEntry(secondId, scope, secondTimestamp, secondData)
            )

        verify(database).query(
            eq(false),
            eq(tableName),
            eq(null),
            stringCaptor.capture(),
            stringArrayCaptor.capture(),
            eq(null),
            eq(null),
            stringCaptor.capture(),
            stringCaptor.capture()
        )

        assertThat(stringCaptor.allValues).containsExactly("scope=?", "id", "$limit")
        assertThat(stringArrayCaptor.allValues)
            .hasSize(1)
            .first().isEqualTo(arrayOf(scope))

        verify(database).closeSafely()
        assertThat(dataCursor.isClosed).isTrue()
    }

    @Test
    fun `get single entry`() {
        assertThat(dbHelper.get(scope))
            .usingRecursiveComparison()
            .isEqualTo(TempCacheEntry(firstId, scope, timestamp, data))

        verify(database).query(
            eq(false),
            eq(tableName),
            eq(null),
            stringCaptor.capture(),
            stringArrayCaptor.capture(),
            eq(null),
            eq(null),
            stringCaptor.capture(),
            stringCaptor.capture()
        )

        assertThat(stringCaptor.allValues).containsExactly("scope=?", "id", "1")
        assertThat(stringArrayCaptor.allValues)
            .hasSize(1)
            .first().isEqualTo(arrayOf(scope))

        verify(database).closeSafely()
        assertThat(dataCursor.isClosed).isTrue()
    }

    @Test
    fun `get multiple entries if db is null`() {
        whenever(dbConnector.openDb()).thenReturn(null)
        assertThat(dbHelper.get(scope, limit)).isEmpty()
    }

    @Test
    fun `get single entry if db is null`() {
        whenever(dbConnector.openDb()).thenReturn(null)
        assertThat(dbHelper.get(scope)).isNull()
    }

    @Test
    fun `get multiple entries if openDb throws`() {
        whenever(dbConnector.openDb()).thenThrow(RuntimeException())
        assertThat(dbHelper.get(scope, limit)).isEmpty()
    }

    @Test
    fun `get single entry if openDb throws`() {
        whenever(dbConnector.openDb()).thenThrow(RuntimeException())
        assertThat(dbHelper.get(scope)).isNull()
    }

    @Test
    fun `get multiple entries if openDb return null database`() {
        whenever(dbConnector.openDb()).thenReturn(null)
        assertThat(dbHelper.get(scope, limit)).isEmpty()
    }

    @Test
    fun `get single entry if openDb return null database`() {
        whenever(dbConnector.openDb()).thenReturn(null)
        assertThat(dbHelper.get(scope)).isNull()
    }

    @Test
    fun `get multiple entries if query request throws`() {
        whenever(
            database.query(
                eq(false),
                eq(tableName),
                eq(null),
                any(),
                any(),
                eq(null),
                eq(null),
                any(),
                any()
            )
        ).thenThrow(RuntimeException())
        assertThat(dbHelper.get(scope, limit)).isEmpty()
        verify(database).closeSafely()
    }

    @Test
    fun `get single entry if query request throws`() {
        whenever(
            database.query(
                eq(false),
                eq(tableName),
                eq(null),
                any(),
                any(),
                eq(null),
                eq(null),
                any(),
                any()
            )
        ).thenThrow(RuntimeException())
        assertThat(dbHelper.get(scope)).isNull()
        verify(database).closeSafely()
    }

    @Test
    fun `get multiple entries if query request return null cursor`() {
        whenever(
            database.query(
                eq(false),
                eq(tableName),
                eq(null),
                any(),
                any(),
                eq(null),
                eq(null),
                any(),
                any()
            )
        ).thenReturn(null)
        assertThat(dbHelper.get(scope, limit)).isEmpty()
        verify(database).closeSafely()
    }

    @Test
    fun `get single entry if query request return null cursor`() {
        whenever(
            database.query(
                eq(false),
                eq(tableName),
                eq(null),
                any(),
                any(),
                eq(null),
                eq(null),
                any(),
                any()
            )
        ).thenReturn(null)
        assertThat(dbHelper.get(scope)).isNull()
        verify(database).closeSafely()
    }

    @Test
    fun `get multiple entries if cursor is empty`() {
        whenever(
            database.query(
                eq(false),
                eq(tableName),
                eq(null),
                any(),
                any(),
                eq(null),
                eq(null),
                any(),
                any()
            )
        ).thenReturn(emptyCursor)
        assertThat(dbHelper.get(scope, limit)).isEmpty()
        verify(database).closeSafely()
        verify(emptyCursor).closeSafely()
    }

    @Test
    fun `get single entry if cursor is empty`() {
        whenever(
            database.query(
                eq(false),
                eq(tableName),
                eq(null),
                any(),
                any(),
                eq(null),
                eq(null),
                any(),
                any()
            )
        ).thenReturn(emptyCursor)
        assertThat(dbHelper.get(scope)).isNull()
        verify(database).closeSafely()
        verify(emptyCursor).closeSafely()
    }

    @Test
    fun remove() {
        dbHelper.remove(idToDelete)
        verify(database).delete(eq(tableName), stringCaptor.capture(), stringArrayCaptor.capture())
        verify(database).closeSafely()

        assertThat(stringCaptor.allValues).containsExactly("id=?")
        assertThat(stringArrayCaptor.allValues).hasSize(1)
        assertThat(stringArrayCaptor.firstValue).containsExactly("$idToDelete")
    }

    @Test
    fun removeOlderThan() {
        dbHelper.removeOlderThan(scope, intervalToDelete)
        verify(database).delete(eq(tableName), stringCaptor.capture(), stringArrayCaptor.capture())
        verify(database).closeSafely()

        assertThat(stringCaptor.allValues).containsExactly("scope=? AND timestamp<?")
        assertThat(stringArrayCaptor.allValues).hasSize(1)
        assertThat(stringArrayCaptor.firstValue).containsExactly(scope, "${now - intervalToDelete}")
    }

    @Test
    fun `remove if database is null`() {
        whenever(dbConnector.openDb()).thenReturn(null)
        dbHelper.remove(idToDelete)
    }

    @Test
    fun `removeOlderThan if database is null`() {
        whenever(dbConnector.openDb()).thenReturn(null)
        dbHelper.removeOlderThan(scope, intervalToDelete)
    }

    @Test
    fun `remove if openDb throws exception`() {
        whenever(dbConnector.openDb()).thenThrow(RuntimeException())
        dbHelper.remove(idToDelete)
    }

    @Test
    fun `removeOlderThan if openDb throws exception`() {
        whenever(dbConnector.openDb()).thenThrow(RuntimeException())
        dbHelper.removeOlderThan(scope, intervalToDelete)
    }

    @Test
    fun `remove if query throws exception`() {
        whenever(database.delete(any(), any(), any())).thenThrow(RuntimeException())
        dbHelper.remove(idToDelete)
        verify(database).closeSafely()
    }

    @Test
    fun `removeOlderThan if query throws exception`() {
        whenever(database.delete(any(), any(), any())).thenThrow(RuntimeException())
        dbHelper.removeOlderThan(scope, intervalToDelete)
        verify(database).closeSafely()
    }
}

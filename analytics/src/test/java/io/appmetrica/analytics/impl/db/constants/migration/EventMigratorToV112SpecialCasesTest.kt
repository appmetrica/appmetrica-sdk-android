package io.appmetrica.analytics.impl.db.constants.migration

import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.coreutils.internal.io.closeSafely
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.constants.migrations.ComponentDatabaseUpgradeScriptToV112
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventModelConverter
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class EventMigratorToV112SpecialCasesTest : CommonTest() {

    private val dropScript = "DROP TABLE IF EXISTS reports"

    private val database = mock<SQLiteDatabase>()

    @get:Rule
    val dbEventModelConverterMockedConstructionRule = MockedConstructionRule(DbEventModelConverter::class.java)

    private lateinit var cursor: Cursor

    private lateinit var eventsMigrator: ComponentDatabaseUpgradeScriptToV112.EventsMigrator
    private lateinit var dbEventModelConverter: DbEventModelConverter

    @Before
    fun setUp() {
        cursor = prepareCursor()
        whenever(database.query(
            "reports",
            null,
            null,
            null,
            null,
            null,
            null,
            "2000"
        )).thenReturn(cursor)

        eventsMigrator = ComponentDatabaseUpgradeScriptToV112.EventsMigrator()
        dbEventModelConverter = dbEventModelConverterMockedConstructionRule.constructionMock.constructed().first()
        whenever(dbEventModelConverter.fromModel(any())).thenReturn(mock())
    }

    @After
    fun tearDown() {
        cursor.closeSafely()
    }

    @Test
    fun `runScript call order`() {
        eventsMigrator.runScript(database)

        inOrder(database) {
            verify(database).execSQL(Constants.EventsTable.CREATE_TABLE)
            verify(database).query(
                "reports",
                null,
                null,
                null,
                null,
                null,
                null,
                "2000"
            )
            verify(database, times(2)).insertOrThrow(eq(Constants.EventsTable.TABLE_NAME), eq(null), any())
            verify(database).execSQL(dropScript)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `runScript if read throw`() {
        whenever(database.query(
            "reports",
            null,
            null,
            null,
            null,
            null,
            null,
            "2000"
        )).thenThrow(RuntimeException())

        eventsMigrator.runScript(database)

        inOrder(database) {
            verify(database).execSQL(Constants.EventsTable.CREATE_TABLE)
            verify(database).query(
                "reports",
                null,
                null,
                null,
                null,
                null,
                null,
                "2000"
            )
            verify(database, never()).insertOrThrow(eq(Constants.EventsTable.TABLE_NAME), eq(null), any())
            verify(database).execSQL(dropScript)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `runScript if no old data`() {
        whenever(database.query(
            "reports",
            null,
            null,
            null,
            null,
            null,
            null,
            "2000"
        )).thenReturn(
            MatrixCursor(
                arrayOf(
                    "session_id", "session_type", "number", "type", "global_number",
                    "time", "custom_type", "name", "value", "number_of_type",
                    "error_environment", "app_environment", "app_environment_revision",
                    "truncated", "encrypting_mode", "profile_id", "first_occurrence_status",
                    "source", "attribution_id_changed", "open_id", "extras"
                )
            )
        )

        eventsMigrator.runScript(database)

        inOrder(database) {
            verify(database).execSQL(Constants.EventsTable.CREATE_TABLE)
            verify(database).query(
                "reports",
                null,
                null,
                null,
                null,
                null,
                null,
                "2000"
            )
            verify(database, never()).insertOrThrow(eq(Constants.EventsTable.TABLE_NAME), eq(null), any())
            verify(database).execSQL(dropScript)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `runScript if some record is wrong`() {
        whenever(dbEventModelConverter.fromModel(any())).thenReturn(null, mock())

        eventsMigrator.runScript(database)

        inOrder(database) {
            verify(database).execSQL(Constants.EventsTable.CREATE_TABLE)
            verify(database).query("reports", null, null, null, null, null, null, "2000")
            verify(database).insertOrThrow(eq(Constants.EventsTable.TABLE_NAME), eq(null), any())
            verify(database).execSQL(dropScript)
            verifyNoMoreInteractions()
        }
    }

    private fun prepareCursor(): Cursor = MatrixCursor(
        arrayOf(
            "session_id", "session_type", "number", "type", "global_number",
            "time", "custom_type", "name", "value", "number_of_type",
            "error_environment", "app_environment", "app_environment_revision",
            "truncated", "encrypting_mode", "profile_id", "first_occurrence_status",
            "source", "attribution_id_changed", "open_id", "extras"
        )
    ).apply {
        addRow(
            arrayOf(
                10_000_000_020L, 0, 15, 1, 3421, 1234242L, 54, "Event name", "Event value", 343, "", "", 23,
                13, 1, "ProfileId", 1, 0, 1, 22, ByteArray(12) {it.toByte()}
            )
        )
        addRow(
            arrayOf(
                10_000_000_030L, 1, 15, 1, 3421, 1234242L, 54, "Event name #2", "Event value #2", 343, "", "", 23,
                13, 1, "ProfileId", 1, 0, 0, 22, ByteArray(12) {it.toByte()}
            )
        )
    }
}

package io.appmetrica.analytics.impl.db.constants.migration

import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.constants.migrations.ComponentDatabaseUpgradeScriptToV112
import io.appmetrica.analytics.impl.db.protobuf.converter.DbSessionModelConverter
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
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
internal class SessionsMigratorToV112SpecialCasesTest : CommonTest() {

    private val dropScript = "DROP TABLE IF EXISTS sessions"

    private val database = mock<SQLiteDatabase>()

    @get:Rule
    val dbSessionModelConverterMockedConstructionRule = MockedConstructionRule(DbSessionModelConverter::class.java)

    private lateinit var cursor: Cursor

    private lateinit var sessionsMigrator: ComponentDatabaseUpgradeScriptToV112.SessionsMigrator
    private lateinit var dbSessionModelConverter: DbSessionModelConverter

    @Before
    fun setUp() {
        cursor = prepareCursor()
        whenever(
            database.query(
                "sessions",
                null,
                null,
                null,
                null,
                null,
                null,
                "200"
            )
        ).thenReturn(cursor)

        sessionsMigrator = ComponentDatabaseUpgradeScriptToV112.SessionsMigrator()
        dbSessionModelConverter = dbSessionModelConverterMockedConstructionRule.constructionMock.constructed().first()
        whenever(dbSessionModelConverter.fromModel(any())).thenReturn(mock())
    }

    @Test
    fun `runScript call order`() {
        sessionsMigrator.runScript(database)

        inOrder(database) {
            verify(database).query(
                "sessions",
                null,
                null,
                null,
                null,
                null,
                null,
                "200"
            )
            verify(database).execSQL(dropScript)
            verify(database).execSQL(Constants.SessionTable.CREATE_TABLE)
            verify(database, times(2)).insertOrThrow(eq(Constants.SessionTable.TABLE_NAME), eq(null), any())
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `runScript if read throw`() {
        whenever(
            database.query(
                "sessions",
                null,
                null,
                null,
                null,
                null,
                null,
                "200"
            )
        ).thenThrow(RuntimeException())

        sessionsMigrator.runScript(database)

        inOrder(database) {
            verify(database).query(
                "sessions",
                null,
                null,
                null,
                null,
                null,
                null,
                "200"
            )
            verify(database).execSQL(dropScript)
            verify(database).execSQL(Constants.SessionTable.CREATE_TABLE)
            verify(database, never()).insertOrThrow(eq(Constants.SessionTable.TABLE_NAME), eq(null), any())
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `runScript if no old data`() {
        whenever(
            database.query(
                "sessions",
                null,
                null,
                null,
                null,
                null,
                null,
                "200"
            )
        ).thenReturn(
            MatrixCursor(
                arrayOf(
                    "ID", "start_time", "report_request_parameters", "server_time_offset", "type",
                    "obtained_before_first_sync"
                )
            )
        )

        sessionsMigrator.runScript(database)

        inOrder(database) {
            verify(database).query(
                "sessions",
                null,
                null,
                null,
                null,
                null,
                null,
                "200"
            )
            verify(database).execSQL(dropScript)
            verify(database).execSQL(Constants.SessionTable.CREATE_TABLE)
            verify(database, never()).insertOrThrow(eq(Constants.SessionTable.TABLE_NAME), eq(null), any())
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `runScript if some record is wrong`() {
        whenever(dbSessionModelConverter.fromModel(any())).thenReturn(null, mock())

        sessionsMigrator.runScript(database)

        inOrder(database) {
            verify(database).query("sessions", null, null, null, null, null, null, "200")
            verify(database).execSQL(dropScript)
            verify(database).execSQL(Constants.SessionTable.CREATE_TABLE)
            verify(database).insertOrThrow(eq(Constants.SessionTable.TABLE_NAME), eq(null), any())
            verifyNoMoreInteractions()
        }
    }

    private fun prepareCursor(): Cursor = MatrixCursor(
        arrayOf(
            "ID", "start_time", "report_request_parameters", "server_time_offset", "type",
            "obtained_before_first_sync"
        )
    ).apply {
        addRow(
            arrayOf(
                100L, 200L, "param#1", 10L, 0L, 0L,
            )
        )
        addRow(
            arrayOf(
                200L, 400L, "params#2", 20L, 1L, 1L
            )
        )
    }
}

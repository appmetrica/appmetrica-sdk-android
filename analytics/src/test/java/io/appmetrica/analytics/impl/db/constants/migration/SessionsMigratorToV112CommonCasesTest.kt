package io.appmetrica.analytics.impl.db.constants.migration

import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.constants.migrations.ComponentDatabaseUpgradeScriptToV112
import io.appmetrica.analytics.impl.db.protobuf.converter.DbSessionModelConverter
import io.appmetrica.analytics.impl.db.session.DbSessionModel
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class SessionsMigratorToV112CommonCasesTest(
    private val description: String,
    private val inputRecord: SessionCursorRecord?,
    private val expectedRecord: SessionCursorRecord?
) : CommonTest() {

    companion object {

        private const val sessionIdValue = 1_000_000_002L
        private const val startTimeValue = 12365434L
        private const val reportRequestParametersValue = "report_request_parameters"
        private const val serverTimeOffsetValue = 2312L
        private const val sessionTypeValue = 1
        private const val obtainedBeforeFirstSyncIntValue = 1

        class SessionCursorRecord(
            val sessionId: Any? = sessionIdValue,
            val startTime: Any? = startTimeValue,
            val reportRequestParameters: Any? = reportRequestParametersValue,
            val serverTimeOffset: Any? = serverTimeOffsetValue,
            val sessionType: Any? = sessionTypeValue,
            val obtainedBeforeFirstSync: Any? = obtainedBeforeFirstSyncIntValue,
            val locationInfo: Any? = "location_info"
        )

        @ParameterizedRobolectricTestRunner.Parameters(name = "[{index}] {0}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf("filled value", SessionCursorRecord(), SessionCursorRecord()),
            // Session Id
            arrayOf(
                "null session Id",
                SessionCursorRecord(sessionId = null),
                null
            ),
            arrayOf(
                "zero session Id",
                SessionCursorRecord(sessionId = 0L),
                null
            ),
            arrayOf(
                "small session id",
                SessionCursorRecord(sessionId = 1_000_000_000 - 1),
                null
            ),
            arrayOf(
                "negative session id",
                SessionCursorRecord(sessionId = -1L),
                null
            ),
            arrayOf("invalid session id", SessionCursorRecord(sessionId = "invalid value"), null),
            // session type
            arrayOf(
                "null session type",
                SessionCursorRecord(sessionType = null),
                null
            ),
            arrayOf(
                "negative session type",
                SessionCursorRecord(sessionType = -1),
                null
            ),
            arrayOf(
                "background session type",
                SessionCursorRecord(sessionType = SessionType.BACKGROUND.code),
                SessionCursorRecord(sessionType = SessionType.BACKGROUND.code)
            ),
            arrayOf(
                "foreground session type",
                SessionCursorRecord(sessionType = SessionType.FOREGROUND.code),
                SessionCursorRecord(sessionType = SessionType.FOREGROUND.code)
            ),
            arrayOf(
                "unknown session type",
                SessionCursorRecord(sessionType = 1000L),
                null
            ),
            arrayOf(
                "invalid session type",
            SessionCursorRecord(sessionType = "invalid"),
            null
            ),
            // reportRequestParameters
            arrayOf(
                "null report request parameters",
                SessionCursorRecord(reportRequestParameters = null),
                null
            ),
            arrayOf(
                "empty report request parameters",
                SessionCursorRecord(reportRequestParameters = ""),
                null
            ),
            arrayOf(
                "some value",
                SessionCursorRecord(reportRequestParameters = "some value"),
                SessionCursorRecord(reportRequestParameters = "some value")
            ),
            // start time
            arrayOf(
                "null start time",
                SessionCursorRecord(startTime = null),
                null
            ),
            arrayOf(
                "negative start time",
                SessionCursorRecord(startTime = -1L),
                null
            ),
            arrayOf(
                "zero start time",
                SessionCursorRecord(startTime = 0),
                null
            ),
            arrayOf(
                "positive start time",
                SessionCursorRecord(startTime = 150L),
                SessionCursorRecord(startTime = 150L)
            ),
            arrayOf(
                "invalid start time",
                SessionCursorRecord(startTime = "Invalid"),
                null
            ),
            // server time offset
            arrayOf(
                "null server time offset",
                SessionCursorRecord(serverTimeOffset = null),
                SessionCursorRecord(serverTimeOffset = 0L)
            ),
            arrayOf(
                "zero server time offset",
                SessionCursorRecord(serverTimeOffset = 0L),
                SessionCursorRecord(serverTimeOffset = 0L)
            ),
            arrayOf(
                "negative server time offset",
                SessionCursorRecord(serverTimeOffset = -1L),
                SessionCursorRecord(serverTimeOffset = -1L)
            ),
            arrayOf(
                "positive server time offset",
                SessionCursorRecord(serverTimeOffset = 100L),
                SessionCursorRecord(serverTimeOffset = 100L),
            ),
            arrayOf(
                "invalid server time offset",
                SessionCursorRecord(serverTimeOffset = "invalid value"),
                null
            ),
            // obtainedBeforeFirstSync
            arrayOf(
                "negative obtainedBeforeFirstSync",
                SessionCursorRecord(obtainedBeforeFirstSync = -1),
                SessionCursorRecord(obtainedBeforeFirstSync = 0)
            ),
            arrayOf(
                "null obtainedBeforeFirstSync",
                SessionCursorRecord(obtainedBeforeFirstSync = null),
                SessionCursorRecord(obtainedBeforeFirstSync = 0)
            ),
            arrayOf(
                "obtainedBeforeFirstSync",
                SessionCursorRecord(obtainedBeforeFirstSync = 0),
                SessionCursorRecord(obtainedBeforeFirstSync = 0)
            ),
            arrayOf(
                "unknown obtainedBeforeFirstSync",
                SessionCursorRecord(obtainedBeforeFirstSync = 100600),
                SessionCursorRecord(obtainedBeforeFirstSync = 0)
            ),
            arrayOf(
                "invalid obtainedBeforeFirstSync value",
                SessionCursorRecord(obtainedBeforeFirstSync = "invalid"),
                null
            )
        )
    }

    @get:Rule
    val dbSessionModelConverterMockedConstructionRule = MockedConstructionRule(DbSessionModelConverter::class.java)

    private val database = mock<SQLiteDatabase>()
    private val dbSessionModelCaptor = argumentCaptor<DbSessionModel>()
    private val contentValues = mock<ContentValues>()

    private lateinit var migrator: ComponentDatabaseUpgradeScriptToV112.SessionsMigrator
    private lateinit var dbSessionModelConverter: DbSessionModelConverter

    @Before
    fun setUp() {
        inputRecord?.let { stubDatabase(it) }

        migrator = ComponentDatabaseUpgradeScriptToV112.SessionsMigrator()
        dbSessionModelConverter = dbSessionModelConverter()
        whenever(dbSessionModelConverter.fromModel(dbSessionModelCaptor.capture())).thenReturn(contentValues)

        migrator.runScript(database)
    }

    @Test
    fun `runScript verify write`() {
        if (expectedRecord != null) {
            verify(database).insertOrThrow(Constants.SessionTable.TABLE_NAME, null, contentValues)
        } else {
            verify(database, never()).insertOrThrow(any(), any(), any())
        }
    }

    @Test
    fun `runScript verify values`() {
        if (expectedRecord != null) {
            assertThat(dbSessionModelCaptor.allValues).hasSize(1)

            ObjectPropertyAssertions(dbSessionModelCaptor.firstValue)
                .checkField("id", expectedRecord.sessionId)
                .checkField("type", expectedRecord.sessionType?.let { SessionType.getByCode(it as Int) })
                .checkField("reportRequestParameters", expectedRecord.reportRequestParameters)
                .checkFieldRecursively<Consumer<DbSessionModel.Description>>("description") { assertions ->
                    assertions
                        .checkField("startTime", expectedRecord.startTime)
                        .checkField("serverTimeOffset", expectedRecord.serverTimeOffset)
                        .checkField(
                            "obtainedBeforeFirstSynchronization",
                            (expectedRecord.obtainedBeforeFirstSync as Int?).toBoolean()
                        )
                }
                .checkAll()
        }
    }

    private fun Int?.toBoolean(): Boolean? = this?.let { this == 1 }

    private fun dbSessionModelConverter(): DbSessionModelConverter {
        assertThat(dbSessionModelConverterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        return dbSessionModelConverterMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun stubDatabase(cursorRecord: SessionCursorRecord) {
        whenever(database.query(
            "sessions",
            null,
            null,
            null,
            null,
            null,
            null,
            "200"
        )).thenReturn(prepareCursor(cursorRecord))
    }

    private fun prepareCursor(cursorRecord: SessionCursorRecord): Cursor = MatrixCursor(
        arrayOf(
            "ID", "start_time", "report_request_parameters", "server_time_offset", "type",
            "obtained_before_first_sync", "location_info"
        )
    ).apply {
        addRow(
            arrayOf(
                cursorRecord.sessionId, cursorRecord.startTime, cursorRecord.reportRequestParameters,
                cursorRecord.serverTimeOffset, cursorRecord.sessionType,
                cursorRecord.obtainedBeforeFirstSync,
                cursorRecord.locationInfo
            )
        )
    }
}

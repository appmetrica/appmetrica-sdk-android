package io.appmetrica.analytics.impl.db.constants.migration

import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.EventSource
import io.appmetrica.analytics.impl.FirstOccurrenceStatus
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.constants.migrations.ComponentDatabaseUpgradeScriptToV112
import io.appmetrica.analytics.impl.db.event.DbEventModel
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventModelConverter
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.LogRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class EventMigratorToV112CommonCasesTest(
    private val description: String,
    private val inputRecord: EventCursorRecord?,
    private val expectedRecord: EventCursorRecord?
) : CommonTest() {

    companion object {

        class EventCursorRecord(
            val sessionId: Any? = 10_000_000_008,
            val sessionType: Any? = SessionType.BACKGROUND.code,
            val numberInSession: Any? = 54L,
            val type: Any? = InternalEvents.EVENT_TYPE_APP_OPEN.typeId,
            val globalNumber: Any? = 1232134L,
            val time: Any? = 555000L,
            val customType: Any? = 1313,
            val name: Any? = "Event name",
            val value: Any? = "Event value",
            val numberOfType: Any? = 76L,
            val errorEnvironment: Any? = "Error environment",
            val appEnvironment: Any? = "App environment",
            val appEnvironmentRevision: Any? = 64L,
            val truncated: Any? = 1132,
            val encryptingMode: Any? = EventEncryptionMode.AES_VALUE_ENCRYPTION.modeId,
            val profileId: Any? = "Profile id",
            val firstOccurrenceStatus: Any? = FirstOccurrenceStatus.FIRST_OCCURRENCE.mStatusCode,
            val source: Any? = EventSource.NATIVE.code,
            val attributionIdChanged: Any? = 1,
            val openId: Any? = 113,
            val extras: ByteArray? = ByteArray(5) { it.toByte() }
        )

        @ParameterizedRobolectricTestRunner.Parameters(name = "[{index}] {0}")
        @JvmStatic
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf("filled value", EventCursorRecord(), EventCursorRecord()),
            // region session Id
            arrayOf("sessionId = null", EventCursorRecord(sessionId = null), null),
            arrayOf("sessionId = negative", EventCursorRecord(sessionId = -1L), null),
            arrayOf("sessionId = 0", EventCursorRecord(sessionId = 0L), null),
            arrayOf("sessionId < 10_000_000_000", EventCursorRecord(sessionId = 10_000_000_000 - 1), null),
            arrayOf("sessionId = string", EventCursorRecord(sessionId = "string value"), null),
            // endregion
            // region session type
            arrayOf(
                "sessionType = foreground",
                EventCursorRecord(sessionType = SessionType.FOREGROUND.code),
                EventCursorRecord(sessionType = SessionType.FOREGROUND.code)
            ),
            arrayOf(
                "sessionType = background",
                EventCursorRecord(sessionType = SessionType.BACKGROUND.code),
                EventCursorRecord(sessionType = SessionType.BACKGROUND.code)
            ),
            arrayOf("sessionType = null", EventCursorRecord(sessionType = null), EventCursorRecord(sessionType = 0)),
            arrayOf("sessionType = unknown", EventCursorRecord(sessionType = -1), null),
            arrayOf("sessionType = string value", EventCursorRecord(sessionType = "string value"), null),
            // endregion
            // region numberInSession
            arrayOf(
                "numberInSession = null",
                EventCursorRecord(numberInSession = null),
                EventCursorRecord(numberInSession = 0L)
            ),
            arrayOf("numberInSession = negative", EventCursorRecord(numberInSession = -1L), null),
            arrayOf(
                "numberInSession = 0",
                EventCursorRecord(numberInSession = 0L),
                EventCursorRecord(numberInSession = 0L)
            ),
            arrayOf("numberInSession = string value", EventCursorRecord(numberInSession = "string value"), null),
            // endregion
            // region type
            arrayOf("type = null", EventCursorRecord(type = null), EventCursorRecord(type = 0)),
            arrayOf("type = negative", EventCursorRecord(type = -1), null),
            arrayOf("type = 0", EventCursorRecord(type = 0), EventCursorRecord(type = 0)),
            arrayOf(
                "type = REGULAR",
                EventCursorRecord(type = InternalEvents.EVENT_TYPE_REGULAR.typeId),
                EventCursorRecord(type = InternalEvents.EVENT_TYPE_REGULAR.typeId)
            ),
            arrayOf("type = UNKNOWN", EventCursorRecord(type = 100500700), null),
            arrayOf("type = string value", EventCursorRecord(type = "string value"), null),
            // endregion
            // region global number
            arrayOf("globalNumber = null", EventCursorRecord(globalNumber = null), EventCursorRecord(globalNumber = 0L)),
            arrayOf("globalNumber = negative", EventCursorRecord(globalNumber = -1L), null),
            arrayOf("globalNumber = 0", EventCursorRecord(globalNumber = 0L), EventCursorRecord(globalNumber = 0L)),
            arrayOf(
                "globalNumber = positive",
                EventCursorRecord(globalNumber = 100_000L),
                EventCursorRecord(globalNumber = 100_000L)
            ),
            arrayOf("global number = string value", EventCursorRecord(globalNumber = "string value"), null),
            // endregion
            // region time
            arrayOf("time = null", EventCursorRecord(time = null), EventCursorRecord(time = 0L)),
            arrayOf("time = negative", EventCursorRecord(time = -1L), null),
            arrayOf("time = 0", EventCursorRecord(time = 0L), EventCursorRecord(time = 0L)),
            arrayOf("time = positive", EventCursorRecord(time = 100_500L), EventCursorRecord(time = 100_500L)),
            arrayOf("time = string value", EventCursorRecord(time = "string value"), null),
            // endregion
            // region customType
            arrayOf("customType = null", EventCursorRecord(customType = null), EventCursorRecord(customType = 0)),
            arrayOf("customType = negative", EventCursorRecord(customType = -1), EventCursorRecord(customType = -1)),
            arrayOf("customType = 0", EventCursorRecord(customType = 0), EventCursorRecord(customType = 0)),
            arrayOf("customType = positive", EventCursorRecord(customType = 100), EventCursorRecord(customType = 100)),
            arrayOf("customType = string value", EventCursorRecord(customType = "string value"), null),
            // endregion
            // region name
            arrayOf("name = null", EventCursorRecord(name = null), EventCursorRecord(name = null)),
            arrayOf("name = empty string", EventCursorRecord(name = ""), EventCursorRecord(name = "")),
            arrayOf("name = some string", EventCursorRecord(name = "name"), EventCursorRecord(name = "name")),
            // endregion
            // region value
            arrayOf("value = null", EventCursorRecord(value = null), EventCursorRecord(value = null)),
            arrayOf("value = empty string", EventCursorRecord(value = ""), EventCursorRecord(value = "")),
            arrayOf(
                "value = valid string",
                EventCursorRecord(value = "valid string"),
                EventCursorRecord(value = "valid string")
            ),
            // endregion
            // region numberOfType
            arrayOf("numberOfType = negative", EventCursorRecord(numberOfType = -1L), null),
            arrayOf("numberOfType = 0", EventCursorRecord(numberOfType = 0L), EventCursorRecord(numberOfType = 0L)),
            arrayOf(
                "numberOfType = positive",
                EventCursorRecord(numberOfType = 17L),
                EventCursorRecord(numberOfType = 17L)
            ),
            arrayOf("numberOfType = string value", EventCursorRecord(numberOfType = "string value"), null),
            // endregion
            // region errorEnvironment
            arrayOf(
                "errorEnvironment = null",
                EventCursorRecord(errorEnvironment = null),
                EventCursorRecord(errorEnvironment = null)
            ),
            arrayOf(
                "errorEnvironment = empty string",
                EventCursorRecord(errorEnvironment = ""),
                EventCursorRecord(errorEnvironment = "")
            ),
            arrayOf(
                "errorEnvironment = valid string",
                EventCursorRecord(errorEnvironment = "errorEnvironment"),
                EventCursorRecord(errorEnvironment = "errorEnvironment")
            ),
            // endregion
            // region appEnvironment
            arrayOf(
                "appEnvironment = null",
                EventCursorRecord(appEnvironment = null),
                EventCursorRecord(appEnvironment = null)
            ),
            arrayOf(
                "appEnvironment = empty string",
                EventCursorRecord(appEnvironment = ""),
                EventCursorRecord(appEnvironment = "")
            ),
            arrayOf(
                "appEnvironment = valid string",
                EventCursorRecord(appEnvironment = "appEnvironment"),
                EventCursorRecord(appEnvironment = "appEnvironment")
            ),
            // endregion
            // region appEnvironmentRevision
            arrayOf(
                "appEnvironmentRevision = negative",
                EventCursorRecord(appEnvironmentRevision = -1L),
                EventCursorRecord(appEnvironmentRevision = -1L)
            ),
            arrayOf(
                "appEnvironmentRevision = 0",
                EventCursorRecord(appEnvironmentRevision = 0L),
                EventCursorRecord(appEnvironmentRevision = 0L)
            ),
            arrayOf(
                "appEnvironmentRevision = positive",
                EventCursorRecord(appEnvironmentRevision = 22L),
                EventCursorRecord(appEnvironmentRevision = 22L)
            ),
            arrayOf(
                "appEnvironmentRevision = string value",
                EventCursorRecord(appEnvironmentRevision = "string value"),
                null
            ),
            // endregion
            // region truncated
            arrayOf("truncated = null", EventCursorRecord(truncated = null), EventCursorRecord(truncated = 0)),
            arrayOf("truncated = negative", EventCursorRecord(truncated = -1), null),
            arrayOf("truncated = 0", EventCursorRecord(truncated = 0), EventCursorRecord(truncated = 0)),
            arrayOf("truncated = positive", EventCursorRecord(truncated = 150), EventCursorRecord(truncated = 150)),
            arrayOf("truncated = string value", EventCursorRecord(truncated = "string value"), null),
            // endregion
            // region encryptingMode
            arrayOf(
                "encryptingMode = NONE",
                EventCursorRecord(encryptingMode = EventEncryptionMode.NONE.modeId),
                EventCursorRecord(encryptingMode = EventEncryptionMode.NONE.modeId)
            ),
            arrayOf(
                "encryptingMode = EXTERNALLY_ENCRYPTED_EVENT_CRYPTER",
                EventCursorRecord(encryptingMode = EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER.modeId),
                EventCursorRecord(encryptingMode = EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER.modeId)
            ),
            arrayOf(
                "encryptingMode = AES_VALUE_ENCRYPTION",
                EventCursorRecord(encryptingMode = EventEncryptionMode.AES_VALUE_ENCRYPTION.modeId),
                EventCursorRecord(encryptingMode = EventEncryptionMode.AES_VALUE_ENCRYPTION.modeId)
            ),
            arrayOf(
                "encryptingMode = null",
                EventCursorRecord(encryptingMode = null),
                EventCursorRecord(encryptingMode = EventEncryptionMode.NONE.modeId)
            ),
            arrayOf(
                "encryptingMode = negative",
                EventCursorRecord(encryptingMode = -1),
                EventCursorRecord(encryptingMode = null)
            ),
            arrayOf(
                "encryptingMode = 0",
                EventCursorRecord(encryptingMode = 0),
                EventCursorRecord(encryptingMode = EventEncryptionMode.NONE.modeId)
            ),
            arrayOf(
                "encryptingMode = unknown value",
                EventCursorRecord(encryptingMode = 100500),
                EventCursorRecord(encryptingMode = null)
            ),
            arrayOf(
                "encryptingMode = wrong format",
                EventCursorRecord(encryptingMode = "wrong format"),
                null
            ),
            // endregion
            // region profileId
            arrayOf("profileId = null", EventCursorRecord(profileId = null), EventCursorRecord(profileId = null)),
            arrayOf("profileId = empty string", EventCursorRecord(profileId = ""), EventCursorRecord(profileId = "")),
            arrayOf(
                "profileId = filled value",
                EventCursorRecord(profileId = "value"),
                EventCursorRecord(profileId = "value")
            ),
            // endregion
            // region firstOccurrenceStatus
            arrayOf(
                "firstOccurrenceStatus = UNKNOWN",
                EventCursorRecord(firstOccurrenceStatus = FirstOccurrenceStatus.UNKNOWN.mStatusCode),
                EventCursorRecord(firstOccurrenceStatus = FirstOccurrenceStatus.UNKNOWN.mStatusCode)
            ),
            arrayOf(
                "firstOccurrenceStatus = FIRST_OCCURRENCE",
                EventCursorRecord(firstOccurrenceStatus = FirstOccurrenceStatus.FIRST_OCCURRENCE.mStatusCode),
                EventCursorRecord(firstOccurrenceStatus = FirstOccurrenceStatus.FIRST_OCCURRENCE.mStatusCode)
            ),
            arrayOf(
                "firstOccurrenceStatus = NON_FIRST_OCCURRENCE",
                EventCursorRecord(firstOccurrenceStatus = FirstOccurrenceStatus.NON_FIRST_OCCURENCE.mStatusCode),
                EventCursorRecord(firstOccurrenceStatus = FirstOccurrenceStatus.NON_FIRST_OCCURENCE.mStatusCode)
            ),
            arrayOf(
                "firstOccurrenceStatus = null",
                EventCursorRecord(firstOccurrenceStatus = null),
                EventCursorRecord(firstOccurrenceStatus = FirstOccurrenceStatus.UNKNOWN.mStatusCode)
            ),
            arrayOf(
                "firstOccurrenceStatus = negative",
                EventCursorRecord(firstOccurrenceStatus = -1),
                EventCursorRecord(firstOccurrenceStatus = null)
            ),
            arrayOf(
                "firstOccurrenceStatus = 0",
                EventCursorRecord(firstOccurrenceStatus = 0),
                EventCursorRecord(firstOccurrenceStatus = FirstOccurrenceStatus.UNKNOWN.mStatusCode)
            ),
            arrayOf(
                "firstOccurrenceStatus = unknown code",
                EventCursorRecord(firstOccurrenceStatus = 100500),
                EventCursorRecord(firstOccurrenceStatus = null)
            ),
            arrayOf(
                "firstOccurrenceStatus = invalid value",
                EventCursorRecord(firstOccurrenceStatus = "string value"),
                null
            ),
            // endregion
            // region source
            arrayOf(
                "source = NATIVE",
                EventCursorRecord(source = EventSource.NATIVE.code),
                EventCursorRecord(source = EventSource.NATIVE.code)
            ),
            arrayOf(
                "source = JS",
                EventCursorRecord(source = EventSource.JS.code),
                EventCursorRecord(source = EventSource.JS.code)
            ),
            arrayOf(
                "source = null",
                EventCursorRecord(source = null),
                EventCursorRecord(source = EventSource.NATIVE.code)
            ),
            arrayOf("source = negative code", EventCursorRecord(source = -1), EventCursorRecord(source = null)),
            arrayOf(
                "source = 0",
                EventCursorRecord(source = 0),
                EventCursorRecord(source = EventSource.NATIVE.code)
            ),
            arrayOf(
                "source = unknown code",
                EventCursorRecord(source = 100500),
                EventCursorRecord(source = null)
            ),
            arrayOf("source = invalid format", EventCursorRecord(source = "invalid format"), null),
            // endregion
            // region attributionIdChanged
            arrayOf(
                "attributionIdChanged = null",
                EventCursorRecord(attributionIdChanged = null),
                EventCursorRecord(attributionIdChanged = 0)
            ),
            arrayOf(
                "attributionIdChanged = negative value",
                EventCursorRecord(attributionIdChanged = -1),
                EventCursorRecord(attributionIdChanged = 0)
            ),
            arrayOf(
                "attributionIdChanged = 0",
                EventCursorRecord(attributionIdChanged = 0),
                EventCursorRecord(attributionIdChanged = 0)
            ),
            arrayOf(
                "attributionIdChanged = 1",
                EventCursorRecord(attributionIdChanged = 1),
                EventCursorRecord(attributionIdChanged = 1)
            ),
            arrayOf(
                "attributionIdChanged = unknown big positive value",
                EventCursorRecord(attributionIdChanged = 100500),
                EventCursorRecord(attributionIdChanged = 0)
            ),
            arrayOf(
                "attributionIdChanged = invalid format",
                EventCursorRecord(attributionIdChanged = "invalid value"),
                null
            ),
            // endregion
            // region openId
            arrayOf("openId = null", EventCursorRecord(openId = null), EventCursorRecord(openId = 0)),
            arrayOf("openId = negative value", EventCursorRecord(openId = -1), null),
            arrayOf("openId = 0", EventCursorRecord(openId = 0), EventCursorRecord(openId = 0)),
            arrayOf("openId = positive", EventCursorRecord(openId = 100500), EventCursorRecord(openId = 100500)),
            arrayOf("openId = invalid format", EventCursorRecord(openId = "invalid format"), null),
            // endregion
            // region extras
            arrayOf("extras = null", EventCursorRecord(extras = null), EventCursorRecord(extras = null)),
            arrayOf(
                "extras = empty array",
                EventCursorRecord(extras = ByteArray(0)),
                EventCursorRecord(extras = ByteArray(0))
            ),
            arrayOf(
                "extras = filled value",
                EventCursorRecord(extras = ByteArray(2) { it.toByte() }),
                EventCursorRecord(extras = ByteArray(2) { it.toByte() })
            )
            // endregion
        )
    }

    @get:Rule
    val logRule = LogRule()

    @get:Rule
    val dbEventModelConverterMockedConstructionRule = MockedConstructionRule(DbEventModelConverter::class.java)

    private val database = mock<SQLiteDatabase>()
    private val dbEventModelCaptor = argumentCaptor<DbEventModel>()
    private val contentValues = mock<ContentValues>()

    private lateinit var migrator: ComponentDatabaseUpgradeScriptToV112.EventsMigrator
    private lateinit var converter: DbEventModelConverter

    @Before
    fun setUp() {
        inputRecord?.let { stubDatabase(it) }

        migrator = ComponentDatabaseUpgradeScriptToV112.EventsMigrator()
        converter = dbEventModelConverter()

        whenever(converter.fromModel(dbEventModelCaptor.capture())).thenReturn(contentValues)

        migrator.runScript(database)
    }

    @Test
    fun runScript() {
        if (expectedRecord == null) {
            verify(database, never()).insertOrThrow(Constants.EventsTable.TABLE_NAME, null, contentValues)
        } else {
            verify(database).insertOrThrow(Constants.EventsTable.TABLE_NAME, null, contentValues)
            assertThat(dbEventModelCaptor.allValues).hasSize(1)

            ObjectPropertyAssertions(dbEventModelCaptor.firstValue)
                .checkField("session", expectedRecord.sessionId)
                .checkField("sessionType", SessionType.getByCode(expectedRecord.sessionType as Int))
                .checkField("numberInSession", expectedRecord.numberInSession)
                .checkField(
                    "type",
                    expectedRecord.type?.let { InternalEvents.valueOf(expectedRecord.type as Int) }
                )
                .checkField("globalNumber", expectedRecord.globalNumber)
                .checkField("time", expectedRecord.time)
                .checkFieldRecursively<DbEventModel.Description>("description") { asserions ->
                    asserions
                        .checkField("customType", expectedRecord.customType)
                        .checkField("name", expectedRecord.name)
                        .checkField("value", expectedRecord.value)
                        .checkField("numberOfType", expectedRecord.numberOfType)
                        .checkField("errorEnvironment", expectedRecord.errorEnvironment)
                        .checkField("appEnvironment", expectedRecord.appEnvironment)
                        .checkField("appEnvironmentRevision", expectedRecord.appEnvironmentRevision)
                        .checkField("truncated", expectedRecord.truncated)
                        .checkField(
                            "encryptingMode",
                            expectedRecord.encryptingMode?.let { EventEncryptionMode.valueOf(it as Int) }
                        )
                        .checkField("profileId", expectedRecord.profileId)
                        .checkField(
                            "firstOccurrenceStatus",
                            expectedRecord.firstOccurrenceStatus?.let {
                                FirstOccurrenceStatus.fromStatusCode(it as Int)
                            }
                        )
                        .checkField("source", expectedRecord.source?.let { EventSource.fromCode(it as Int) })
                        .checkField("attributionIdChanged", expectedRecord.attributionIdChanged == 1)
                        .checkField("openId", expectedRecord.openId)
                        .checkField("extras", expectedRecord.extras)
                        .checkFieldsAreNull("locationInfo", "connectionType", "cellularConnectionType")
                }
                .checkAll()
        }
    }

    private fun stubDatabase(record: EventCursorRecord) {
        whenever(database.query("reports", null, null, null, null, null, null, "2000"))
            .thenReturn(prepareCursor(record))
    }

    private fun prepareCursor(record: EventCursorRecord): Cursor = MatrixCursor(
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
                record.sessionId, record.sessionType, record.numberInSession, record.type, record.globalNumber,
                record.time, record.customType, record.name, record.value, record.numberOfType,
                record.errorEnvironment, record.appEnvironment, record.appEnvironmentRevision,
                record.truncated, record.encryptingMode, record.profileId, record.firstOccurrenceStatus,
                record.source, record.attributionIdChanged, record.openId, record.extras
            )
        )
    }

    private fun dbEventModelConverter(): DbEventModelConverter {
        assertThat(dbEventModelConverterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(dbEventModelConverterMockedConstructionRule.argumentInterceptor.flatArguments()).hasSize(1)
        return dbEventModelConverterMockedConstructionRule.constructionMock.constructed().first()
    }
}

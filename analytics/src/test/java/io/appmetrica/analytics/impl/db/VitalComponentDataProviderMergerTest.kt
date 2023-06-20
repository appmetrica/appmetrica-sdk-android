package io.appmetrica.analytics.impl.db

import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.ParameterizedRobolectricTestRunner

private const val FIRST_EVENT_DONE = "first_event_done"
private const val INIT_EVENT_DONE = "init_event_done"
private const val KEY_REPORT_REQUEST_ID = "report_request_id"
private const val KEY_GLOBAL_NUMBER = "global_number"
private const val KEY_NUMBERS_OF_TYPE = "numbers_of_type"
private const val KEY_SESSION_ID = "session_id"
private const val KEY_REFERRER_HANDLED = "referrer_handled"
private const val KEY_OPEN_ID = "open_id"
private const val KEY_ATTRIBUTION_ID = "attribution_id"
private const val KEY_LAST_MIGRATION_API_LEVEL = "last_migration_api_level"

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class VitalComponentDataProviderMergerTest(
    private val key: String,
    private val primaryValue: Any?,
    private val backupValue: Any?,
    private val expectedValue: Any?,
    private val description: String
) : CommonTest() {

    companion object {

        private val firstJson = JSONObject().put("first", "value")
        private val secondJson = JSONObject().put("second", "value")

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{index}-{4}")
        fun data(): Collection<Array<Any?>> = listOf(
            //region first_event_done
            arrayOf(FIRST_EVENT_DONE, true, null, true, "Filled \"first_event_done\" in primary"),
            arrayOf(FIRST_EVENT_DONE, null, true, true, "Filled \"first_event_done\" in backup"),
            arrayOf(FIRST_EVENT_DONE, false, true, true, "Filled \"first_event_done\" in primary and backup"),
            arrayOf(FIRST_EVENT_DONE, null, null, false, "Missing \"first_event_done\" in primary and backup"),
            //endregion
            //region init_event_done
            arrayOf(INIT_EVENT_DONE, true, null, true, "Filled \"init_event_done\" in primary"),
            arrayOf(INIT_EVENT_DONE, null, true, true, "Filled \"init_event_done\" in backup"),
            arrayOf(INIT_EVENT_DONE, false, true, true, "Filled \"init_event_done\" in primary and backup"),
            arrayOf(INIT_EVENT_DONE, null, null, false, "Missing \"init_event_done\" in primary and backup"),
            //endregion
            //region report_request_id
            arrayOf(KEY_REPORT_REQUEST_ID, 10, null, 10, "Filled \"report_request_id\" in primary"),
            arrayOf(KEY_REPORT_REQUEST_ID, null, 20, 20, "Filled \"report_request_id\" in backup"),
            arrayOf(KEY_REPORT_REQUEST_ID, 10, 20, 20, "Filled \"report_request_id\" in primary and backup"),
            arrayOf(KEY_REPORT_REQUEST_ID, null, null, -1, "Missing \"report_request_id\" in primary and backup"),
            //endregion
            //region global_number
            arrayOf(KEY_GLOBAL_NUMBER, 10L, null, 10L, "Filled \"global_number\" in primary"),
            arrayOf(KEY_GLOBAL_NUMBER, null, 20L, 20L, "Filled \"global_number\" in backup"),
            arrayOf(KEY_GLOBAL_NUMBER, 10L, 20L, 20L, "Filled \"global_number\" in primary and backup"),
            arrayOf(KEY_GLOBAL_NUMBER, null, null, 0L, "Missing \"global_number\" in primary and backup"),
            //endregion
            //region numbers_of_type
            arrayOf(KEY_NUMBERS_OF_TYPE, firstJson, null, firstJson, "Filled \"numbers_of_type\" in primary"),
            arrayOf(KEY_NUMBERS_OF_TYPE, null, secondJson, secondJson, "Filled \"numbers_of_type\" in backup"),
            arrayOf(KEY_NUMBERS_OF_TYPE, firstJson, secondJson, secondJson, "Filled \"numbers_of_type\" in primary and backup"),
            arrayOf(KEY_NUMBERS_OF_TYPE, null, null, null, "Missing \"numbers_of_type\" in primary and backup"),
            //endregion
            //region session_id
            arrayOf(KEY_SESSION_ID, 10L, null, 10L, "Filled \"session_id\" in primary"),
            arrayOf(KEY_SESSION_ID, null, 20L, 20L, "Filled \"session_id\" in backup"),
            arrayOf(KEY_SESSION_ID, 10L, 20L, 20L, "Filled \"session_id\" in primary and backup"),
            arrayOf(KEY_SESSION_ID, null, null, -1L, "Missing \"session_id\" in primary and backup"),
            //endregion
            //region referrer_handled
            arrayOf(KEY_REFERRER_HANDLED, true, null, true, "Filled \"referrer_handled\" in primary"),
            arrayOf(KEY_REFERRER_HANDLED, null, true, true, "Filled \"referrer_handled\" in backup"),
            arrayOf(KEY_REFERRER_HANDLED, false, true, true, "Filled \"referrer_handled\" in primary and backup"),
            arrayOf(KEY_REFERRER_HANDLED, null, null, false, "Missing \"referrer_handled\" in primary and backup"),
            //endregion
            //region open_id
            arrayOf(KEY_OPEN_ID, 10, null, 10, "Filled \"open_id\" in primary"),
            arrayOf(KEY_OPEN_ID, null, 20, 20, "Filled \"open_id\" in backup"),
            arrayOf(KEY_OPEN_ID, 10, 20, 20, "Filled \"open_id\" in primary and backup"),
            arrayOf(KEY_OPEN_ID, null, null, 1, "Missing \"open_id\" in primary and backup"),
            //endregion
            //region attribution_id
            arrayOf(KEY_ATTRIBUTION_ID, 10, null, 10, "Filled \"attribution_id\" in primary"),
            arrayOf(KEY_ATTRIBUTION_ID, null, 20, 20, "Filled \"attribution_id\" in backup"),
            arrayOf(KEY_ATTRIBUTION_ID, 10, 20, 20, "Filled \"attribution_id\" in primary and backup"),
            arrayOf(KEY_ATTRIBUTION_ID, null, null, 1, "Missing \"attribution_id\" in primary and backup"),
            //endregion
            //region last_migration_api_level
            arrayOf(KEY_LAST_MIGRATION_API_LEVEL, 10, null, 10, "Filled \"last_migration_api_level\" in primary"),
            arrayOf(KEY_LAST_MIGRATION_API_LEVEL, null, 20, 20, "Filled \"last_migration_api_level\" in backup"),
            arrayOf(KEY_LAST_MIGRATION_API_LEVEL, 10, 20, 20, "Filled \"last_migration_api_level\" in primary and backup"),
            arrayOf(KEY_LAST_MIGRATION_API_LEVEL, null, null, 0, "Missing \"last_migration_api_level\" in primary and backup"),
            //endregion
        )
    }

    private val primaryDataSource = mock<VitalDataSource>()
    private val backupDataSource = mock<VitalDataSource>()

    private lateinit var vitalComponentDataProvider: VitalComponentDataProvider
    private lateinit var merger: VitalDataProviderStateMerger

    @get:Rule
    val vitalDataProviderMockedRule = MockedConstructionRule(VitalDataProvider::class.java)

    private val primaryOddKey = "Primary odd key"
    private val backupOddKey = "Backup odd key"

    private val primaryEmptyJson = JSONObject().put(primaryOddKey, "Some value")
    private val backupEmptyJson = JSONObject().put(backupOddKey, "Some value")

    private val apiKey = "API_KEY"

    private val primaryFilledJson = JSONObject()
        .put("first_event_done", true)
        .put("init_event_done", true)
        .put("report_request_id", 1232)
        .put("global_number", 231)
        .put("numbers_of_type", 512)
        .put("session_id", 123L)
        .put("referrer_handled", true)
        .put("open_id", 324321)
        .put("attribution_id", 213)
        .put("last_migration_api_level", 343)
        .put(primaryOddKey, "Some value")

    private val backupFilledJson = JSONObject()
        .put("first_event_done", false)
        .put("init_event_done", false)
        .put("report_request_id", 1231232)
        .put("global_number", 23111)
        .put("numbers_of_type", 5122)
        .put("session_id", 112223L)
        .put("referrer_handled", false)
        .put("open_id", 324343221)
        .put("attribution_id", 213433)
        .put("last_migration_api_level", 343123)
        .put(backupOddKey, "some value")

    @Before
    fun setUp() {
        vitalComponentDataProvider = VitalComponentDataProvider(
            primaryDataSource,
            backupDataSource,
            apiKey
        )

        val arguments = vitalDataProviderMockedRule.argumentInterceptor.flatArguments()
        assertThat(arguments.size).isEqualTo(4)
        assertThat(arguments[0]).isEqualTo(primaryDataSource)
        assertThat(arguments[1]).isEqualTo(backupDataSource)
        assertThat(arguments[2]).isEqualTo("[VitalComponentDataProvider-$apiKey]")
        merger = arguments[3] as VitalDataProviderStateMerger
    }

    @Test
    fun partiallyFilled() {
        primaryEmptyJson.put(key, primaryValue)
        backupEmptyJson.put(key, backupValue)
        val result = merger.merge(primaryEmptyJson, backupEmptyJson)
        assertThat(result.opt(key)).isEqualTo(expectedValue)
        assertThat(result.has(primaryOddKey)).isFalse()
        assertThat(result.has(backupOddKey)).isFalse()
    }

    @Test
    fun filled() {
        primaryFilledJson.put(key, primaryValue)
        backupFilledJson.put(key, backupValue)
        val result = merger.merge(primaryFilledJson, backupFilledJson)
        assertThat(result.opt(key)).isEqualTo(expectedValue)
        assertThat(result.has(primaryOddKey)).isFalse()
        assertThat(result.has(backupOddKey)).isFalse()
    }
}

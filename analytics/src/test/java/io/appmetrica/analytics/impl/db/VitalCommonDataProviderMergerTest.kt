package io.appmetrica.analytics.impl.db

import android.util.Base64
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
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

private const val KEY_DEVICE_ID = "device_id"
private const val KEY_DEVICE_ID_HASH = "device_id_hash"
private const val KEY_REFERRER = "referrer"
private const val KEY_REFERRER_CHECKED = "referrer_checked"
private const val KEY_LAST_MIGRATION_API_LEVEL = "last_migration_api_level"

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class VitalCommonDataProviderMergerTest(
    private val key: String,
    private val primaryValue: Any?,
    private val backupValue: Any?,
    private val expectedValue: Any?,
    private val description: String
) : CommonTest() {

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{index}-{4}")
        fun data(): Collection<Array<Any?>> = listOf(
            //region device_Id
            arrayOf(KEY_DEVICE_ID, "FILLED", null, "FILLED", "Filled \"device_Id\" in primary"),
            arrayOf(KEY_DEVICE_ID, null, "FILLED", "FILLED", "Filled \"device_Id\" in backup"),
            arrayOf(KEY_DEVICE_ID, "PRIMARY", "BACKUP", "BACKUP", "Filled \"device_Id\" in primary and backup"),
            arrayOf(KEY_DEVICE_ID, null, null, null, "Missing \"device_Id\" in primary and backup"),
            //endregion
            //region device_id_hash
            arrayOf(KEY_DEVICE_ID_HASH, "FILLED", null, "FILLED", "Filled \"device_id_hash\" in primary"),
            arrayOf(KEY_DEVICE_ID_HASH, null, "FILLED", "FILLED", "Filled \"device_id_hash\" in backup"),
            arrayOf(KEY_DEVICE_ID_HASH, "PRIMARY", "BACKUP", "BACKUP", "Filled \"device_id_hash\" in primary and backup"),
            arrayOf(KEY_DEVICE_ID_HASH, null, null, null, "Missing \"device_id_hash\" in primary and backup"),
            //endregion
            //region referrer
            arrayOf(KEY_REFERRER, "PRIMARY", null, "PRIMARY", "Filled \"referrer\" in primary"),
            arrayOf(KEY_REFERRER, null, "BACKUP", "BACKUP", "Filled \"referrer\" in backup"),
            arrayOf(KEY_REFERRER, "PRIMARY", "BACKUP", "BACKUP", "Filled \"referrer\" in primary and backup"),
            arrayOf(KEY_REFERRER, null, null, null, "Missing \"referrer\" in primary and backup"),
            //endregion
            //region referrer_checked
            arrayOf(KEY_REFERRER_CHECKED, true, null, true, "Filled \"referrer_checked\" in primary"),
            arrayOf(KEY_REFERRER_CHECKED, null, true, true, "Filled \"referrer_checked\" in backup"),
            arrayOf(KEY_REFERRER_CHECKED, false, true, true, "Filled \"referrer_checked\" in primary and backup"),
            arrayOf(KEY_REFERRER_CHECKED, null, null, false, "Missing \"referrer_checked\" in primary and backup"),
            //endregion
            //region last_migration_api_level
            arrayOf(KEY_LAST_MIGRATION_API_LEVEL, 10, null, 10, "Filled \"last_migration_api_level\" in primary"),
            arrayOf(KEY_LAST_MIGRATION_API_LEVEL, null, 20, 20, "Filled \"last_migration_api_level\" in backup"),
            arrayOf(KEY_LAST_MIGRATION_API_LEVEL, 10, 20, 20, "Filled \"last_migration_api_level\" in primary and backup"),
            arrayOf(KEY_LAST_MIGRATION_API_LEVEL, null, null, -1, "Missing \"last_migration_api_level\" in primary and backup"),
            //endregion
        )
    }

    private val primaryDataSource = mock<VitalDataSource>()
    private val backupDataSource = mock<VitalDataSource>()

    private lateinit var vitalCommonDataProvider: VitalCommonDataProvider
    private lateinit var merger: VitalDataProviderStateMerger

    @get:Rule
    val vitalDataProviderMockedRule = MockedConstructionRule(VitalDataProvider::class.java)

    private val primaryOddKey = "primary odd key"
    private val backupOddKey = "backup odd key"

    private val primaryEmptyJson = JSONObject().put(primaryOddKey, "odd value")
    private val backupEmptyJson = JSONObject().put(backupOddKey, "odd value")

    private val referrer = ReferrerInfo("referrer", 21, 32, ReferrerInfo.Source.GP)

    private val primaryFilledJson = JSONObject()
        .put("device_id", "adsasdasdas")
        .put("device_id_hash", "5344212331")
        .put("location_id", 4L)
        .put("lbs_id", 6L)
        .put("referrer", String(Base64.encode(referrer.toProto(), 0)))
        .put("referrer_checked", true)
        .put("location_request_id", 45343L)
        .put("last_migration_api_level", 12323432)
        .put(primaryOddKey, "some value")

    private val backupFilledJson = JSONObject()
        .put("device_id", "sdfsdf")
        .put("device_id_hash", "3213")
        .put("location_id", 43L)
        .put("lbs_id", 6123L)
        .put("referrer", String(Base64.encode(referrer.toProto(), 0)))
        .put("referrer_checked", true)
        .put("location_request_id", 415343L)
        .put("last_migration_api_level", 122323432)
        .put(backupOddKey, "some value")

    @Before
    fun setUp() {
        vitalCommonDataProvider = VitalCommonDataProvider(primaryDataSource, backupDataSource)

        val arguments = vitalDataProviderMockedRule.argumentInterceptor.flatArguments()
        assertThat(arguments.size).isEqualTo(4)
        assertThat(arguments[0]).isEqualTo(primaryDataSource)
        assertThat(arguments[1]).isEqualTo(backupDataSource)
        assertThat(arguments[2]).isEqualTo("[VitalCommonDataProvider]")
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

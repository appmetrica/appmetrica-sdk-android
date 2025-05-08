package io.appmetrica.analytics.impl.db

import android.util.Base64
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.skyscreamer.jsonassert.JSONAssert

@RunWith(RobolectricTestRunner::class)
internal class VitalCommonDataProviderTest : CommonTest() {

    @get:Rule
    val vitalDataProviderMockedConstructionRule = MockedConstructionRule(VitalDataProvider::class.java)

    private val primaryDataSource = mock<VitalDataSource>()
    private val backupDataSource = mock<VitalDataSource>()
    private val jsonCaptor = argumentCaptor<JSONObject>()

    private lateinit var vitalCommonDataProvider: VitalCommonDataProvider

    private val deviceId = "3746876eee"
    private val deviceIdHash = "9879879www"
    private val lastMigrationApiLevel = 44
    private val referrer = ReferrerInfo("referrer", 21, 32, ReferrerInfo.Source.GP)
    private val filledJson = JSONObject()
        .put("device_id", deviceId)
        .put("device_id_hash", deviceIdHash)
        .put("referrer", String(Base64.encode(referrer.toProto(), 0)))
        .put("referrer_checked", true)
        .put("last_migration_api_level", lastMigrationApiLevel)

    @Before
    fun setUp() {
        vitalCommonDataProvider = VitalCommonDataProvider(primaryDataSource, backupDataSource)
    }

    @Test
    fun filledJsonFromStorage() {
        val filledJsonWithExtraKey = JSONObject(filledJson.toString()).put("bad_key", "bad_value")
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJsonWithExtraKey)

        // todo https://nda.ya.ru/t/H8WfYGSJ6Njj76
        val softly = SoftAssertions()
        softly.assertThat(vitalCommonDataProvider.deviceId).isEqualTo(deviceId)
        softly.assertThat(vitalCommonDataProvider.deviceIdHash).isEqualTo(deviceIdHash)
        softly.assertThat(vitalCommonDataProvider.referrer).isEqualToComparingFieldByField(referrer)
        softly.assertThat(vitalCommonDataProvider.referrerChecked).isTrue
        softly.assertThat(vitalCommonDataProvider.lastMigrationApiLevel).isEqualTo(lastMigrationApiLevel)
        softly.assertAll()
    }

    @Test
    fun setDeviceId() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        vitalCommonDataProvider.deviceId = "new device id"
        val expectedJson = JSONObject(filledJson.toString()).put("device_id", "new device id")
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun setDeviceIdHash() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        vitalCommonDataProvider.deviceIdHash = "new device id hash"
        val expectedJson = JSONObject(filledJson.toString()).put("device_id_hash", "new device id hash")
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun setLastMigrationApiLevel() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        vitalCommonDataProvider.lastMigrationApiLevel = 12
        val expectedJson = JSONObject(filledJson.toString()).put("last_migration_api_level", 12)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun setReferrerChecked() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        vitalCommonDataProvider.referrerChecked = true
        val expectedJson = JSONObject(filledJson.toString()).put("referrer_checked", true)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun setReferrer() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        val referrer = ReferrerInfo("referrer2", 99, 88, ReferrerInfo.Source.HMS)
        vitalCommonDataProvider.referrer = referrer
        val expectedJson = JSONObject(filledJson.toString())
            .put("referrer", String(Base64.encode(referrer.toProto(), 0)))
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun badReferrer() {
        val inputJson = JSONObject(filledJson.toString()).also {
            it.put("referrer", "bad string")
        }
        wheneverVitalDataProviderGetOrLoad().thenReturn(inputJson)
        assertThat(vitalCommonDataProvider.referrer).isNull()
    }

    @Test
    fun setInitialState() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)

        val newDeviceId = "888999rrr"
        val newDeviceIdHash = "444555qqq"
        val newLastMigrationApiLevel = 21
        val newReferrer = ReferrerInfo("new referrer", 7, 8, ReferrerInfo.Source.HMS)

        vitalCommonDataProvider.setInitialState(
            newDeviceId,
            newDeviceIdHash,
            String(Base64.encode(newReferrer.toProto(), 0)),
            false,
            newLastMigrationApiLevel
        )

        val expectedJson = JSONObject().put("device_id", newDeviceId)
            .put("device_id_hash", newDeviceIdHash)
            .put("referrer", String(Base64.encode(newReferrer.toProto(), 0)))
            .put("referrer_checked", false)
            .put("last_migration_api_level", newLastMigrationApiLevel)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun setInitialStateBadReferrer() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)

        val newReferrer = "bad string"

        vitalCommonDataProvider.setInitialState(
            deviceId,
            deviceIdHash,
            newReferrer,
            true,
            lastMigrationApiLevel
        )

        val savedJson = interceptSavedJson()
        wheneverVitalDataProviderGetOrLoad().thenReturn(savedJson)
        assertThat(vitalCommonDataProvider.referrer).isNull()
        assertThat(interceptSavedJson().optString("referrer")).isEqualTo("bad string")
    }

    @Test
    fun emptyInputJson() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(JSONObject())
        checkDefaultValues()
    }

    private fun wheneverVitalDataProviderGetOrLoad() =
        whenever(vitalDataProvider().getOrLoadData())

    private fun vitalDataProvider() = vitalDataProviderMockedConstructionRule.constructionMock.constructed()[0]

    private fun interceptSavedJson(): JSONObject {
        verify(vitalDataProvider()).save(jsonCaptor.capture())
        return jsonCaptor.firstValue
    }

    // todo https://nda.ya.ru/t/H8WfYGSJ6Njj76
    private fun checkDefaultValues() {
        val softly = SoftAssertions()
        softly.assertThat(vitalCommonDataProvider.deviceId).isNull()
        softly.assertThat(vitalCommonDataProvider.deviceIdHash).isNull()
        softly.assertThat(vitalCommonDataProvider.referrer).isNull()
        softly.assertThat(vitalCommonDataProvider.referrerChecked).isFalse
        softly.assertThat(vitalCommonDataProvider.lastMigrationApiLevel).isEqualTo(-1)
        softly.assertAll()
    }
}

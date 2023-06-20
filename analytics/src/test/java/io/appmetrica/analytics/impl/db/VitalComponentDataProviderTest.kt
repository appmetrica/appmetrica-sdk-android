package io.appmetrica.analytics.impl.db

import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
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
internal class VitalComponentDataProviderTest : CommonTest() {

    @get:Rule
    val vitalDataProviderMockedConstructionRule = MockedConstructionRule(VitalDataProvider::class.java)

    private val primaryDataSource = mock<VitalDataSource>()
    private val backupDataSource = mock<VitalDataSource>()
    private val jsonCaptor = argumentCaptor<JSONObject>()

    private val apiKey = "some_api_key"

    private lateinit var vitalComponentDataProvider: VitalComponentDataProvider

    private val reportRequestId = 7778
    private val globalNumber = 22233L
    private val sessionId = 1113L
    private val numbersOfType = JSONObject().put("12", 2).put("8", 10)
    private val openId = 987
    private val attributionId = 432
    private val lastMigrationApiLevel = 43

    private val filledJson = JSONObject()
        .put("first_event_done", true)
        .put("init_event_done", true)
        .put("report_request_id", reportRequestId)
        .put("global_number", globalNumber)
        .put("numbers_of_type", numbersOfType)
        .put("session_id", sessionId)
        .put("referrer_handled", true)
        .put("open_id", openId)
        .put("attribution_id", attributionId)
        .put("last_migration_api_level", lastMigrationApiLevel)

    @Before
    fun setUp() {
        vitalComponentDataProvider = VitalComponentDataProvider(primaryDataSource, backupDataSource, "API_KEY")
    }

    @Test
    fun filledJsonFromStorage() {
        val filledJsonWithExtraKey = JSONObject(filledJson.toString()).put("bad_key", "bad_value")
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJsonWithExtraKey)

        // todo https://nda.ya.ru/t/H8WfYGSJ6Njj76
        val softly = SoftAssertions()
        softly.assertThat(vitalComponentDataProvider.isInitEventDone).isTrue
        softly.assertThat(vitalComponentDataProvider.isFirstEventDone).isTrue
        softly.assertThat(vitalComponentDataProvider.sessionId).isEqualTo(sessionId)
        softly.assertThat(vitalComponentDataProvider.reportRequestId).isEqualTo(reportRequestId)
        softly.assertThat(vitalComponentDataProvider.referrerHandled).isTrue
        softly.assertThat(vitalComponentDataProvider.openId).isEqualTo(openId)
        softly.assertThat(vitalComponentDataProvider.attributionId).isEqualTo(attributionId)
        softly.assertThat(vitalComponentDataProvider.globalNumber).isEqualTo(globalNumber)
        softly.assertThat(vitalComponentDataProvider.lastMigrationApiLevel).isEqualTo(lastMigrationApiLevel)
        JSONAssert.assertEquals(numbersOfType, vitalComponentDataProvider.numbersOfType, true)
        softly.assertAll()
    }

    @Test
    fun setHasInitEvent() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        vitalComponentDataProvider.isInitEventDone = false
        val expectedJson = JSONObject(filledJson.toString()).put("init_event_done", false)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun setHasFirstEvent() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        vitalComponentDataProvider.isFirstEventDone = false
        val expectedJson = JSONObject(filledJson.toString()).put("first_event_done", false)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun setReportRequestId() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        vitalComponentDataProvider.reportRequestId = 1414
        val expectedJson = JSONObject(filledJson.toString()).put("report_request_id", 1414)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun setGlobalNumber() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        vitalComponentDataProvider.globalNumber = 1414
        val expectedJson = JSONObject(filledJson.toString()).put("global_number", 1414)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun setSessionId() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        vitalComponentDataProvider.sessionId = 1414
        val expectedJson = JSONObject(filledJson.toString()).put("session_id", 1414)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun setReferrerHandled() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        vitalComponentDataProvider.referrerHandled = false
        val expectedJson = JSONObject(filledJson.toString()).put("referrer_handled", false)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun setNumbersOfType() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        val newNumbersOfType = JSONObject().put("new key", "new value")
        vitalComponentDataProvider.numbersOfType = newNumbersOfType
        val expectedJson = JSONObject(filledJson.toString()).put("numbers_of_type", newNumbersOfType)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun setLastMigrationApiLevel() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        vitalComponentDataProvider.lastMigrationApiLevel = 5454
        val expectedJson = JSONObject(filledJson.toString()).put("last_migration_api_level", 5454)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun incrementOpenIdNoInitial() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(JSONObject())
        vitalComponentDataProvider.incrementOpenId()
        val expectedJson = JSONObject().put("open_id", 2)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun incrementOpenIdInitialIsNonZero() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        vitalComponentDataProvider.incrementOpenId()
        val expectedJson = JSONObject(filledJson.toString()).put("open_id", openId + 1)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun incrementAttributionIdNoInitial() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(JSONObject())
        vitalComponentDataProvider.incrementAttributionId()
        val expectedJson = JSONObject().put("attribution_id", 2)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun incrementAttributionIdInitialIsNonZero() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)
        vitalComponentDataProvider.incrementAttributionId()
        val expectedJson = JSONObject(filledJson.toString()).put("attribution_id", attributionId + 1)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun setInitialState() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(filledJson)

        val newReportRequestId = 3334
        val newGlobalNumber = 777888
        val newSessionId = 5432L
        val newNumbersOfType = JSONObject().put("43", 20).put("7", 2)
        val newOpenId = 545
        val newAttributionId = 232
        val newLastMigrationApiLevel = 21

        vitalComponentDataProvider.setInitialState(
            isFirstEventDone = false,
            isInitEventDone = false,
            reportRequestId = newReportRequestId,
            globalNumber = newGlobalNumber,
            sessionId = newSessionId,
            numbersOfType = newNumbersOfType,
            openId = newOpenId,
            attributionId = newAttributionId,
            lastMigrationApiLevel = newLastMigrationApiLevel,
            referrerHandled = false
        )

        val expectedJson = JSONObject()
            .put("first_event_done", false)
            .put("init_event_done", false)
            .put("report_request_id", newReportRequestId)
            .put("global_number", newGlobalNumber)
            .put("numbers_of_type", newNumbersOfType)
            .put("session_id", newSessionId)
            .put("referrer_handled", false)
            .put("open_id", newOpenId)
            .put("attribution_id", newAttributionId)
            .put("last_migration_api_level", newLastMigrationApiLevel)
        JSONAssert.assertEquals(expectedJson.toString(), interceptSavedJson(), true)
    }

    @Test
    fun emptyJsonInStorage() {
        wheneverVitalDataProviderGetOrLoad().thenReturn(JSONObject())
        checkDefaultValues()
    }

    private fun wheneverVitalDataProviderGetOrLoad() =
        whenever(vitalDataProvider().getOrLoadData())

    private fun vitalDataProvider() = vitalDataProviderMockedConstructionRule.constructionMock.constructed()[0]

    private fun interceptSavedJson() : JSONObject {
        verify(vitalDataProvider()).save(jsonCaptor.capture())
        return jsonCaptor.firstValue
    }

    // todo https://nda.ya.ru/t/H8WfYGSJ6Njj76
    private fun checkDefaultValues() {
        val softly = SoftAssertions()
        softly.assertThat(vitalComponentDataProvider.isFirstEventDone).isFalse
        softly.assertThat(vitalComponentDataProvider.isInitEventDone).isFalse
        softly.assertThat(vitalComponentDataProvider.attributionId).isEqualTo(1)
        softly.assertThat(vitalComponentDataProvider.openId).isEqualTo(1)
        softly.assertThat(vitalComponentDataProvider.globalNumber).isEqualTo(0)
        softly.assertThat(vitalComponentDataProvider.lastMigrationApiLevel).isEqualTo(0)
        softly.assertThat(vitalComponentDataProvider.numbersOfType).isNull()
        softly.assertThat(vitalComponentDataProvider.referrerHandled).isFalse
        softly.assertThat(vitalComponentDataProvider.reportRequestId).isEqualTo(-1)
        softly.assertThat(vitalComponentDataProvider.sessionId).isEqualTo(-1)
        softly.assertAll()
    }

}

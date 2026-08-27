package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.protobuf.backend.EventProto
import io.appmetrica.analytics.impl.request.DbNetworkTaskConfig
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.telephony.TelephonyDataProvider
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ReportMessageBuilderTest : CommonTest() {

    private val uuid = "test-uuid"
    private val deviceId = "test-device-id"
    private val certificates = listOf("cert1", "cert2")
    private val autoCollectedDataSubscribers = setOf("sub1")

    private val emptyDbRequestConfig = DbNetworkTaskConfig()
    private val dbRequestConfig = DbNetworkTaskConfig(
        JsonHelper.OptJSONObject("""{"uId": "db-uuid", "dId": "db-device-id"}""")
    )

    private val requestConfig = mock<ReportRequestConfig> {
        on { uuid } doReturn uuid
        on { deviceId } doReturn deviceId
        on { autoCollectedDataSubscribers } doReturn autoCollectedDataSubscribers
    }

    private val telephonyDataProvider = mock<TelephonyDataProvider>()

    private val builder = ReportMessageBuilder(telephonyDataProvider)

    private fun buildSessionData(
        sessions: List<EventProto.ReportMessage.Session> = emptyList(),
        environment: JSONObject = JSONObject(),
    ) = ReportSessionData(sessions, emptyList(), environment)

    @Test
    fun `build falls back to requestConfig uuid and deviceId when dbRequestConfig has null fields`() {
        val result = builder.build(buildSessionData(), emptyDbRequestConfig, requestConfig, certificates)
        assertThat(result.reportRequestParameters.uuid).isEqualTo(uuid)
        assertThat(result.reportRequestParameters.deviceId).isEqualTo(deviceId)
    }

    @Test
    fun `build prefers dbRequestConfig uuid and deviceId when non-empty`() {
        val result = builder.build(buildSessionData(), dbRequestConfig, requestConfig, certificates)
        assertThat(result.reportRequestParameters.uuid).isEqualTo("db-uuid")
        assertThat(result.reportRequestParameters.deviceId).isEqualTo("db-device-id")
    }

    @Test
    fun `build sets sessions and certificates`() {
        val sessionData = buildSessionData(
            sessions = listOf(EventProto.ReportMessage.Session(), EventProto.ReportMessage.Session())
        )

        val result = builder.build(sessionData, emptyDbRequestConfig, requestConfig, certificates)

        assertThat(result.sessions?.size).isEqualTo(2)
        assertThat(result.certificatesSha1Fingerprints?.toList()).containsExactlyElementsOf(certificates)
    }

    @Test
    fun `build sets additionalApiKeys from autoCollectedDataSubscribers`() {
        val result = builder.build(buildSessionData(), emptyDbRequestConfig, requestConfig, certificates)
        assertThat(result.additionalApiKeys?.size).isEqualTo(1)
        assertThat(String(result.additionalApiKeys!![0])).isEqualTo("sub1")
    }

    @Test
    fun `build sets appEnvironment and null for empty JSONObject`() {
        val env = JSONObject().apply { put("key1", "value1"); put("key2", "value2") }

        val resultWithEnv = builder.build(
            buildSessionData(environment = env), emptyDbRequestConfig, requestConfig, certificates
        )
        val resultEmpty = builder.build(
            buildSessionData(environment = JSONObject()), emptyDbRequestConfig, requestConfig, certificates
        )

        assertThat(resultWithEnv.appEnvironment?.size).isEqualTo(2)
        assertThat(resultEmpty.appEnvironment).isNull()
    }

    @Test
    fun `extractEnvironment returns null for empty JSONObject and correct variables for non-empty`() {
        val emptyResult = builder.extractEnvironment(JSONObject())
        assertThat(emptyResult).isNull()

        val json = JSONObject().apply { put("env_key", "env_value") }
        val result = builder.extractEnvironment(json)
        assertThat(result).isNotNull
        assertThat(result!!.size).isEqualTo(1)
        assertThat(result[0].name).isEqualTo("env_key")
        assertThat(result[0].value).isEqualTo("env_value")
    }
}

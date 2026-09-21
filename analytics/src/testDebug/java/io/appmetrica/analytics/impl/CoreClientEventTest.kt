package io.appmetrica.analytics.impl

import android.annotation.SuppressLint
import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.protobuf.backend.EventStart
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.data.RandomStringGenerator
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.skyscreamer.jsonassert.JSONAssert
import java.nio.charset.StandardCharsets

@SuppressLint("RobolectricUsage") // Bundle / SystemTimeProvider in constructors
@RunWith(RobolectricTestRunner::class)
internal class CoreClientEventTest : CommonTest() {

    private val logger: PublicLogger = mock()

    @Rule
    @JvmField
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val currentTimeMillis = 100500L
    private val currentElapsedRealtime = 200500L

    @Rule
    @JvmField
    val systemTimeProviderRule = MockedConstructionRule(SystemTimeProvider::class.java) { mock, _ ->
        whenever(mock.currentTimeMillis()).thenReturn(currentTimeMillis)
        whenever(mock.elapsedRealtime()).thenReturn(currentElapsedRealtime)
    }

    private val providers = ArrayList<String>()

    @Test
    fun permissionsReportVariants() {
        assertPermissionsJson(
            CoreServiceEvent.formPermissionsReportData(
                mock(),
                listOf(PermissionState("1", false), PermissionState("2", true)),
                BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.RARE, true),
                "expected string",
                listOf("gps", "passive")
            ),
            JSONObject()
                .put(
                    "permissions",
                    JSONArray()
                        .put(JSONObject().put("name", "1").put("granted", false))
                        .put(JSONObject().put("name", "2").put("granted", true))
                )
                .put(
                    "background_restrictions",
                    JSONObject()
                        .put("background_restricted", true)
                        .put("app_standby_bucket", "expected string")
                )
                .put("available_providers", JSONArray().put("gps").put("passive"))
        )

        assertPermissionsJson(
            CoreServiceEvent.formPermissionsReportData(mock(), emptyList(), null, null, providers),
            JSONObject()
                .put("permissions", JSONArray())
                .put("background_restrictions", JSONObject())
                .put("available_providers", JSONArray())
        )

        assertPermissionsJson(
            CoreServiceEvent.formPermissionsReportData(
                mock(),
                emptyList(),
                BackgroundRestrictionsState(null, false),
                null,
                providers
            ),
            JSONObject()
                .put("permissions", JSONArray())
                .put("background_restrictions", JSONObject().put("background_restricted", false))
                .put("available_providers", JSONArray())
        )

        assertPermissionsJson(
            CoreServiceEvent.formPermissionsReportData(
                mock(),
                emptyList(),
                BackgroundRestrictionsState(BackgroundRestrictionsState.AppStandByBucket.RARE, null),
                "rare",
                providers
            ),
            JSONObject()
                .put("permissions", JSONArray())
                .put("background_restrictions", JSONObject().put("app_standby_bucket", "rare"))
                .put("available_providers", JSONArray())
        )

        assertPermissionsJson(
            CoreServiceEvent.formPermissionsReportData(
                mock(),
                emptyList(),
                BackgroundRestrictionsState(null, null),
                null,
                providers
            ),
            JSONObject()
                .put("permissions", JSONArray())
                .put("background_restrictions", JSONObject())
                .put("available_providers", JSONArray())
        )
    }

    @Test
    fun featuresReport() {
        val features = JSONObject()
            .put(
                "features",
                JSONArray().put(
                    JSONObject()
                        .put("name", "feature.name")
                        .put("version", 1)
                        .put("required", false)
                )
            )
        JSONAssert.assertEquals(
            features,
            JSONObject(CoreServiceEvent.formFeaturesReportData(mock(), features.toString()).value),
            true
        )

        val empty = JSONObject().put("features", JSONArray())
        JSONAssert.assertEquals(
            empty,
            JSONObject(CoreServiceEvent.formFeaturesReportData(mock(), empty.toString()).value),
            true
        )
    }

    @Test
    fun creationElapsedRealtimeDefaultsAndSetter() {
        assertThat(CoreClientEvent().creationElapsedRealtime).isEqualTo(currentElapsedRealtime)
        assertThat(
            CoreClientEvent().apply {
                value = "Test value"
                name = "Test event"
                type = 234
            }.creationElapsedRealtime
        ).isEqualTo(currentElapsedRealtime)

        val expected = 2453543L
        val report = CoreClientEvent().apply { creationElapsedRealtime = expected }
        assertThat(report.creationElapsedRealtime).isEqualTo(expected)
    }

    @Test
    fun creationElapsedRealtimeRoundTripViaIpc() {
        val expected = 45345435L
        val report = CoreClientEvent().apply { creationElapsedRealtime = expected }
        val bundle = EventIpcCodec.toBundle(EventIpcData.fromCoreClientEvent(logger, report), Bundle())
        assertThat(CoreServiceEvent.fromIpcData(EventIpcCodec.fromBundle(bundle)).creationElapsedRealtime)
            .isEqualTo(expected)
    }

    @Test
    @Throws(InvalidProtocolBufferNanoException::class)
    fun formNewSessionReportValue() {
        val buildId = "12345678"
        assertThat(
            String(
                EventStart.Value.parseFrom(
                    CoreServiceEvent.formSessionStartReportData(CoreServiceEvent(), buildId).valueBytes
                ).buildId
            )
        ).isEqualTo(buildId)

        assertThat(
            EventStart.Value.parseFrom(
                CoreServiceEvent.formSessionStartReportData(CoreServiceEvent(), null).valueBytes
            ).buildId
        ).isEmpty()
    }

    @Test
    fun creationTimestampDefaultsAndSetter() {
        assertThat(
            CoreClientEvent().apply {
                value = "Test value"
                name = "Test event"
                type = 0
            }.creationTimestamp
        ).isEqualTo(currentTimeMillis)

        val expected = 3454534L
        val report = CoreClientEvent().apply { creationTimestamp = expected }
        assertThat(report.creationTimestamp).isEqualTo(expected)
    }

    @Test
    fun creationTimestampRoundTripViaIpc() {
        val expected = 353454565L
        val report = CoreClientEvent().apply { creationTimestamp = expected }
        val bundle = EventIpcCodec.toBundle(EventIpcData.fromCoreClientEvent(logger, report), Bundle())
        assertThat(CoreServiceEvent.fromIpcData(EventIpcCodec.fromBundle(bundle)).creationTimestamp)
            .isEqualTo(expected)
    }

    @Test
    fun valueAndValueBytes() {
        val bytes = byteArrayOf(1, 2, 3, 4, 10, 11, 12, 13, 14, 15, 21)
        val report = CoreClientEvent().apply { valueBytes = bytes }
        assertThat(report.valueBytes).isEqualTo(bytes)
        assertThat(report.value).isEqualTo(String(bytes, StandardCharsets.UTF_8))

        report.value = null
        assertThat(report.value).isNull()
        assertThat(report.valueBytes).isNull()

        report.valueBytes = null
        assertThat(report.value).isNull()
        assertThat(report.valueBytes).isNull()
    }

    @Test
    fun formUpdatePreActivationConfig() {
        assertThat(CoreClientEvent.formUpdatePreActivationConfig().type).isEqualTo(
            InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG.typeId
        )
    }

    @Test
    fun formJsInitEvent() {
        val value = "event value"
        val result = CoreClientEvent.formJsInitEvent(value)
        assertThat(result.source).isEqualTo(EventSource.JS)
        assertThat(result.type).isEqualTo(InternalEvents.EVENT_TYPE_WEBVIEW_SYNC.typeId)
        assertThat(result.name).isEmpty()
        assertThat(result.value).isEqualTo(value)
    }

    @Test
    fun toStringVariants() {
        SoftAssertions().apply {
            assertThat(CoreClientEvent().toString())
                .contains("event: ")
                .contains("type: " + InternalEvents.EVENT_TYPE_INIT.info)
                .contains("value: ")
        }.assertAll()

        val eventName = "test name"
        val eventValue = "test value"
        val filled = CoreClientEvent().apply {
            type = InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT.typeId
            name = eventName
            value = eventValue
        }
        SoftAssertions().apply {
            assertThat(filled.toString())
                .contains("event: $eventName")
                .contains("type: " + InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT.info)
                .contains("value: $eventValue")
        }.assertAll()

        val fittingValue = RandomStringGenerator(500).nextString()
        val longValue = fittingValue + "aaaaabbbbb"
        val longReport = CoreClientEvent().apply {
            type = InternalEvents.EVENT_TYPE_REGULAR.typeId
            name = eventName
            value = longValue
        }
        SoftAssertions().apply {
            assertThat(longReport.toString())
                .contains("event: $eventName")
                .contains("type: " + InternalEvents.EVENT_TYPE_REGULAR.info)
                .contains("value: $fittingValue")
                .doesNotContain(longValue)
        }.assertAll()
    }

    private fun assertPermissionsJson(serviceEvent: CoreServiceEvent, expected: JSONObject) {
        JSONAssert.assertEquals(expected, JSONObject(serviceEvent.value), true)
    }

    @Test
    fun formJsInitEventUsesNonePolicy() {
        val value = "v".repeat(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE + 100)
        val event = CoreClientEvent.formJsInitEvent(value)
        SoftAssertions().apply {
            assertThat(event.trimPolicy).isEqualTo(EventTrimPolicy.NONE)
            assertThat(event.value).isEqualTo(value)
            assertThat(event.source).isEqualTo(EventSource.JS)
        }.assertAll()
    }

    @Test
    fun formJsEventUsesStandardPolicy() {
        val event = CoreClientEvent.formJsEvent("name", "value")
        SoftAssertions().apply {
            assertThat(event.trimPolicy).isEqualTo(EventTrimPolicy.STANDARD)
            assertThat(event.type).isEqualTo(InternalEvents.EVENT_TYPE_REGULAR.typeId)
            assertThat(event.source).isEqualTo(EventSource.JS)
        }.assertAll()
    }

    @Test
    fun formAppEnvironmentChangedUsesNonePolicy() {
        val event = CoreClientEvent.formAppEnvironmentChangedReport("k", "v")
        assertThat(event.trimPolicy).isEqualTo(EventTrimPolicy.NONE)
        assertThat(event.type).isEqualTo(InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED.typeId)
    }

    @Test
    fun formAdRevenueEventSetsDomainBytesTruncatedWithoutTrimmingValue() {
        val bytes = ByteArray(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE + 5) { 1 }
        val adRevenue = mock<io.appmetrica.analytics.impl.revenue.ad.AdRevenueWrapper>()
        whenever(adRevenue.getDataToSend()).thenReturn(bytes to 77)
        val event = CoreClientEvent.formAdRevenueEvent(adRevenue)
        SoftAssertions().apply {
            assertThat(event.valueBytes).isEqualTo(bytes)
            assertThat(event.bytesTruncated).isEqualTo(77)
            assertThat(event.trimPolicy).isEqualTo(EventTrimPolicy.STANDARD)
        }.assertAll()
    }
}

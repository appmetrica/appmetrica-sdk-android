package io.appmetrica.analytics.impl

import android.annotation.SuppressLint
import android.os.Bundle
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.util.function.Predicate

@SuppressLint("RobolectricUsage") // Bundle usage
@RunWith(RobolectricTestRunner::class)
internal class EventIpcCodecTest : CommonTest() {

    private val logger: PublicLogger = mock()

    @get:Rule
    val systemTimeProviderRule = constructionRule<SystemTimeProvider> {
        on { currentTimeMillis() } doReturn CURRENT_TIME_MILLIS
        on { elapsedRealtime() } doReturn CURRENT_ELAPSED_REALTIME
    }

    private val codec = EventIpcCodec

    private fun readEvent(bundle: Bundle): CoreServiceEvent {
        return CoreServiceEvent.fromIpcData(codec.fromBundle(bundle))
    }

    private fun checkExtrasContent(
        assertions: ObjectPropertyAssertions<CoreServiceEvent>,
        expected: Map<String, ByteArray>,
    ) = assertions.checkFieldMatchPredicate(
        "extras",
        Predicate { actual: MutableMap<String, ByteArray> ->
            actual.size == expected.size && expected.all { (key, value) ->
                actual[key]?.contentEquals(value) == true
            }
        }
    )

    @Test
    fun toBundleWritesFlatKeys() {
        val report = CoreClientEvent().apply {
            value = "value"
            name = "name"
            type = InternalEvents.EVENT_TYPE_REGULAR.typeId
            customType = 7
            bytesTruncated = 3
            profileID = "pid"
            eventEnvironment = "env"
            creationElapsedRealtime = 11L
            creationTimestamp = 22L
            source = EventSource.JS
            payload = Bundle().apply { putString("p", "v") }
            extras = hashMapOf("e" to byteArrayOf(9))
            valueProtocolVersion = 2
        }

        val bundle = codec.toBundle(EventIpcData.fromCoreClientEvent(logger, report), Bundle())

        assertThat(bundle.containsKey(EventIpcBundleKeys.TYPE)).isTrue
        assertThat(bundle.getString(EventIpcBundleKeys.EVENT)).isEqualTo("name")
        assertThat(bundle.getByteArray(EventIpcBundleKeys.VALUE)).isEqualTo("value".toByteArray())
        assertThat(bundle.getInt(EventIpcBundleKeys.TYPE)).isEqualTo(InternalEvents.EVENT_TYPE_REGULAR.typeId)
        assertThat(bundle.getInt(EventIpcBundleKeys.CUSTOM_TYPE)).isEqualTo(7)
        assertThat(bundle.getInt(EventIpcBundleKeys.TRUNCATED)).isEqualTo(3)
        assertThat(bundle.getString(EventIpcBundleKeys.PROFILE_ID)).isEqualTo("pid")
        assertThat(bundle.getString(EventIpcBundleKeys.ENVIRONMENT)).isEqualTo("env")
        assertThat(bundle.getLong(EventIpcBundleKeys.CREATION_ELAPSED_REALTIME)).isEqualTo(11L)
        assertThat(bundle.getLong(EventIpcBundleKeys.CREATION_TIMESTAMP)).isEqualTo(22L)
        assertThat(bundle.getInt(EventIpcBundleKeys.SOURCE)).isEqualTo(EventSource.JS.code)
        assertThat(bundle.getBundle(EventIpcBundleKeys.PAYLOAD)!!.getString("p")).isEqualTo("v")
        assertThat(bundle.getBundle(EventIpcBundleKeys.EXTRAS)!!.getByteArray("e")).isEqualTo(byteArrayOf(9))
        assertThat(bundle.getInt(EventIpcBundleKeys.VALUE_PROTOCOL_VERSION)).isEqualTo(2)
    }

    @Test
    fun fromBundleReadsWrittenFields() {
        val payload = Bundle().apply { putString("p", "v") }
        val extras = hashMapOf("e" to byteArrayOf(9))
        val report = CoreClientEvent().apply {
            value = "v"
            name = "n"
            type = 15
            customType = 2
            bytesTruncated = 3
            profileID = "pid"
            eventEnvironment = "env"
            creationElapsedRealtime = 11L
            creationTimestamp = 22L
            source = EventSource.JS
            this.payload = payload
            this.extras = extras
            valueProtocolVersion = 1
        }
        val event = readEvent(codec.toBundle(EventIpcData.fromCoreClientEvent(logger, report), Bundle()))

        Assertions.ObjectPropertyAssertions(event)
            .withIgnoredFields("systemTimeProvider", "value", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("name", "n")
            .checkField("valueBytesStorage", "v".toByteArray())
            .checkField("type", 15)
            .checkField("customType", 2)
            .checkField("bytesTruncated", 3)
            .checkField("profileID", "pid")
            .checkField("eventEnvironment", "env")
            .checkField("creationElapsedRealtime", 11L)
            .checkField("creationTimestamp", 22L)
            .checkField("source", EventSource.JS)
            .checkField("payload", payload)
            .checkField("valueProtocolVersion", 1)
            .let { assertions -> checkExtrasContent(assertions, extras) }
            .checkAll()
    }

    @Test
    fun fromBundleWithoutTypeIsUndefined() {
        val event = readEvent(Bundle())

        Assertions.ObjectPropertyAssertions(event)
            .withIgnoredFields("systemTimeProvider", "value", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("type", InternalEvents.EVENT_TYPE_UNDEFINED.typeId)
            .checkField("valueBytesStorage", ByteArray(0))
            .checkField("customType", 0)
            .checkField("bytesTruncated", 0)
            .checkField("creationElapsedRealtime", 0L)
            .checkField("creationTimestamp", 0L)
            .let { assertions -> checkExtrasContent(assertions, emptyMap()) }
            .checkFieldIsNull("name")
            .checkFieldIsNull("eventEnvironment")
            .checkFieldIsNull("profileID")
            .checkFieldIsNull("source")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("valueProtocolVersion")
            .checkAll()

        assertThat(event.isUndefinedType).isTrue
    }

    @Test
    fun valueNullNormalizedToEmpty() {
        val report = CoreClientEvent().apply {
            type = 1
            value = null
            creationElapsedRealtime = 0L
            creationTimestamp = 0L
        }
        val event = readEvent(codec.toBundle(EventIpcData.fromCoreClientEvent(logger, report), Bundle()))

        Assertions.ObjectPropertyAssertions(event)
            .withIgnoredFields("systemTimeProvider", "value", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("type", 1)
            .checkField("customType", 0)
            .checkField("valueBytesStorage", ByteArray(0))
            .checkField("name", StringUtils.EMPTY)
            .checkField("bytesTruncated", 0)
            .checkField("creationElapsedRealtime", 0L)
            .checkField("creationTimestamp", 0L)
            .let { assertions -> checkExtrasContent(assertions, emptyMap()) }
            .checkFieldIsNull("eventEnvironment")
            .checkFieldIsNull("profileID")
            .checkFieldIsNull("source")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("valueProtocolVersion")
            .checkAll()
    }

    @Test
    fun fromBundleWithoutTypeReadsFieldsAsIs() {
        val data = codec.fromBundle(Bundle())

        assertThat(data.type).isEqualTo(InternalEvents.EVENT_TYPE_UNDEFINED.typeId)
        assertThat(data.name).isNull()
        assertThat(data.value).isNull()
    }

    @Test
    fun fromCoreClientEventTrimsLongNameAndValue() {
        val longName = "n".repeat(EventLimitationProcessor.EVENT_NAME_MAX_LENGTH + 40)
        val longValue = ByteArray(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE + 12) { 7 }
        val report = CoreClientEvent().apply {
            name = longName
            valueBytes = longValue
            type = InternalEvents.EVENT_TYPE_REGULAR.typeId
            trimPolicy = EventTrimPolicy.STANDARD
        }

        val data = EventIpcData.fromCoreClientEvent(logger, report)

        assertThat(data.name!!.length).isEqualTo(EventLimitationProcessor.EVENT_NAME_MAX_LENGTH)
        assertThat(data.value!!.size).isEqualTo(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE)
        assertThat(data.bytesTruncated).isGreaterThan(0)
        assertThat(report.name).isEqualTo(longName)
        assertThat(report.valueBytes).isEqualTo(longValue)
    }

    @Test
    fun fromCoreClientEventNoneDoesNotTrim() {
        val longName = "n".repeat(EventLimitationProcessor.EVENT_NAME_MAX_LENGTH + 40)
        val longValue = ByteArray(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE + 12) { 7 }
        val report = CoreClientEvent().apply {
            name = longName
            valueBytes = longValue
            type = InternalEvents.EVENT_TYPE_WEBVIEW_SYNC.typeId
            bytesTruncated = 5
            trimPolicy = EventTrimPolicy.NONE
        }

        val data = EventIpcData.fromCoreClientEvent(logger, report)

        assertThat(data.name).isEqualTo(longName)
        assertThat(data.value).isEqualTo(longValue)
        assertThat(data.bytesTruncated).isEqualTo(5)
    }

    @Test
    fun fromCoreClientEventAccumulatesBytesTruncatedWithValueDelta() {
        val longValue = ByteArray(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE + 9) { 4 }
        val report = CoreClientEvent().apply {
            valueBytes = longValue
            type = InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT.typeId
            bytesTruncated = 55
            trimPolicy = EventTrimPolicy.STANDARD
        }

        val data = EventIpcData.fromCoreClientEvent(logger, report)

        assertThat(data.value!!.size).isEqualTo(EventLimitationProcessor.REPORT_VALUE_MAX_SIZE)
        val expectedValueDelta = longValue.size - data.value!!.size
        assertThat(data.bytesTruncated).isEqualTo(55 + expectedValueDelta)
    }

    private companion object {
        private const val CURRENT_TIME_MILLIS = 100_500L
        private const val CURRENT_ELAPSED_REALTIME = 200_500L
    }
}

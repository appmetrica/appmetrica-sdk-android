package io.appmetrica.analytics.impl

import android.annotation.SuppressLint
import android.os.Bundle
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.robolectric.RobolectricTestRunner
import java.util.function.Predicate

@SuppressLint("RobolectricUsage") // Bundle usage
@RunWith(RobolectricTestRunner::class)
internal class EventIpcCodecTest : CommonTest() {

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
        val report = CounterReport("value", "name", InternalEvents.EVENT_TYPE_REGULAR.typeId).apply {
            customType = 7
            bytesTruncated = 3
            setProfileID("pid")
            setEventEnvironment("env")
            setCreationEllapsedRealtime(11L)
            setCreationTimestamp(22L)
            setSource(EventSource.JS)
            setPayload(Bundle().apply { putString("p", "v") })
            extras = hashMapOf("e" to byteArrayOf(9))
            valueProtocolVersion = 2
        }

        val bundle = codec.toBundle(EventIpcData.fromCounterReport(report), Bundle())

        assertThat(bundle.containsKey(EventIpcBundleKeys.TYPE)).isTrue
        assertThat(bundle.getString(EventIpcBundleKeys.EVENT)).isEqualTo("name")
        assertThat(bundle.getString(EventIpcBundleKeys.VALUE)).isEqualTo("value")
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
        val report = CounterReport("v", "n", 15).apply {
            customType = 2
            bytesTruncated = 3
            setProfileID("pid")
            setEventEnvironment("env")
            setCreationEllapsedRealtime(11L)
            setCreationTimestamp(22L)
            setSource(EventSource.JS)
            setPayload(payload)
            this.extras = extras
            valueProtocolVersion = 1
        }
        val event = readEvent(codec.toBundle(EventIpcData.fromCounterReport(report), Bundle()))

        Assertions.ObjectPropertyAssertions(event)
            .withIgnoredFields("systemTimeProvider", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("name", "n")
            .checkField("value", "v")
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
            .withIgnoredFields("systemTimeProvider", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("type", InternalEvents.EVENT_TYPE_UNDEFINED.typeId)
            .checkField("value", "")
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
        val report = CounterReport().apply {
            type = 1
            value = null
            setCreationEllapsedRealtime(0L)
            setCreationTimestamp(0L)
        }
        val event = readEvent(codec.toBundle(EventIpcData.fromCounterReport(report), Bundle()))

        Assertions.ObjectPropertyAssertions(event)
            .withIgnoredFields("systemTimeProvider", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("type", 1)
            .checkField("customType", 0)
            .checkField("value", "")
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

    private companion object {
        private const val CURRENT_TIME_MILLIS = 100_500L
        private const val CURRENT_ELAPSED_REALTIME = 200_500L
    }
}

package io.appmetrica.analytics.impl

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcel
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.client.ClientConfiguration
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.utils.limitation.SimpleMapLimitation
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.gradle.androidtestutils.rules.ContextRule
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

@SuppressLint("RobolectricUsage") // Parcel / Bundle marshalling
@RunWith(RobolectricTestRunner::class)
internal class EventIpcDataContractTest : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    @get:Rule
    val systemTimeProviderRule = constructionRule<SystemTimeProvider> {
        on { currentTimeMillis() } doReturn CURRENT_TIME_MILLIS
        on { elapsedRealtime() } doReturn CURRENT_ELAPSED_REALTIME
    }

    private val codec = EventIpcCodec

    private fun write(report: CounterReport, bundle: Bundle = Bundle()): Bundle {
        return codec.toBundle(EventIpcData.fromCounterReport(report), bundle)
    }

    private fun read(bundle: Bundle): CoreServiceEvent {
        return CoreServiceEvent.fromIpcData(codec.fromBundle(bundle))
    }

    private fun marshal(bundle: Bundle): Bundle {
        val parcel = Parcel.obtain()
        return try {
            parcel.writeBundle(bundle)
            parcel.setDataPosition(0)
            parcel.readBundle(CounterConfiguration::class.java.classLoader)!!
        } finally {
            parcel.recycle()
        }
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

    private fun checkPayload(
        assertions: ObjectPropertyAssertions<CoreServiceEvent>,
        verifier: (Bundle) -> Boolean,
    ) = assertions.checkFieldMatchPredicate(
        "payload",
        Predicate { payload: Bundle? ->
            payload != null && verifier(payload)
        }
    )

    @Test
    fun filledReportRoundTripThroughParcel() {
        val type = InternalEvents.EVENT_TYPE_CUSTOM_EVENT.typeId
        val customType = 8
        val value = "value"
        val eventEnvironment = "eventEnvironment"
        val event = "event"
        val bytesTruncated = 20
        val profileId = "profileId"
        val creationElapsedRealtime = 21212121L
        val creationTimestamp = 32323232L
        val source = EventSource.JS
        val extras = mapOf("extra key" to byteArrayOf(1, 2, 3, 4, 5))
        val payload = Bundle().apply {
            putString("key1", "value1")
            putInt("key2", 10)
            putBundle("nested", Bundle().apply { putString("inner", "ok") })
        }

        val report = CounterReport().apply {
            this.type = type
            this.customType = customType
            this.value = value
            setEventEnvironment(eventEnvironment)
            name = event
            this.bytesTruncated = bytesTruncated
            setProfileID(profileId)
            setCreationEllapsedRealtime(creationElapsedRealtime)
            setCreationTimestamp(creationTimestamp)
            setSource(source)
            setPayload(payload)
            this.extras = HashMap(extras)
            valueProtocolVersion = 2
        }

        val decoded = read(marshal(write(report)))

        Assertions.ObjectPropertyAssertions(decoded)
            .withIgnoredFields("systemTimeProvider", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("type", type)
            .checkField("customType", customType)
            .checkField("value", value)
            .checkField("eventEnvironment", eventEnvironment)
            .checkField("name", event)
            .checkField("bytesTruncated", bytesTruncated)
            .checkField("profileID", profileId)
            .checkField("creationElapsedRealtime", creationElapsedRealtime)
            .checkField("creationTimestamp", creationTimestamp)
            .checkField("source", source)
            .checkField("valueProtocolVersion", 2)
            .let { assertions -> checkExtrasContent(assertions, extras) }
            .let { assertions ->
                checkPayload(assertions) { actual ->
                    actual.getString("key1") == "value1" &&
                        actual.getInt("key2") == 10 &&
                        actual.getBundle("nested")?.getString("inner") == "ok"
                }
            }
            .checkAll()
    }

    @Test
    fun payloadWithSdkParcelableRoundTripsThroughParcel() {
        val identifiersData = IdentifiersData(
            listOf("uuid", "deviceid"),
            mapOf("clid0" to "0"),
            null,
            true,
        )
        val payload = Bundle().apply {
            putParcelable(IdentifiersData.BUNDLE_KEY, identifiersData)
        }

        val report = CounterReport().apply {
            type = InternalEvents.EVENT_TYPE_REGULAR.typeId
            setPayload(payload)
        }

        val decoded = read(marshal(write(report)))

        Assertions.ObjectPropertyAssertions(decoded)
            .withIgnoredFields("systemTimeProvider", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("type", InternalEvents.EVENT_TYPE_REGULAR.typeId)
            .checkField("customType", 0)
            .checkField("value", StringUtils.EMPTY)
            .checkField("name", StringUtils.EMPTY)
            .checkField("bytesTruncated", 0)
            .checkField("creationElapsedRealtime", CURRENT_ELAPSED_REALTIME)
            .checkField("creationTimestamp", CURRENT_TIME_MILLIS)
            .let { checkExtrasContent(it, emptyMap()) }
            .checkFieldIsNull("eventEnvironment")
            .checkFieldIsNull("profileID")
            .checkFieldIsNull("source")
            .checkFieldIsNull("valueProtocolVersion")
            .let { assertions ->
                checkPayload(assertions) { actual ->
                    val restored = actual.getParcelable(IdentifiersData.BUNDLE_KEY) as IdentifiersData?
                    restored != null &&
                        restored.identifiersList == listOf("uuid", "deviceid") &&
                        restored.clidsFromClientForVerification == mapOf("clid0" to "0") &&
                        restored.isForceRefreshConfiguration
                }
            }
            .checkAll()
    }

    @Test
    fun nullOptionalFieldsNormalized() {
        val report = CounterReport().apply {
            type = InternalEvents.EVENT_TYPE_REGULAR.typeId
            value = null
            name = null
            setEventEnvironment(null)
            setProfileID(null)
            setSource(null)
            setPayload(null)
            setCreationEllapsedRealtime(0L)
            setCreationTimestamp(0L)
            extras = HashMap()
            valueProtocolVersion = null
        }

        val decoded = read(marshal(write(report)))

        Assertions.ObjectPropertyAssertions(decoded)
            .withIgnoredFields("systemTimeProvider", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("type", InternalEvents.EVENT_TYPE_REGULAR.typeId)
            .checkField("customType", 0)
            .checkField("value", "")
            .checkField("bytesTruncated", 0)
            .checkField("creationElapsedRealtime", 0L)
            .checkField("creationTimestamp", 0L)
            .let { checkExtrasContent(it, emptyMap()) }
            .checkFieldIsNull("name")
            .checkFieldIsNull("eventEnvironment")
            .checkFieldIsNull("profileID")
            .checkFieldIsNull("source")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("valueProtocolVersion")
            .checkAll()
    }

    @Test
    fun coexistsWithClientConfigurationInSameBundle() {
        val report = CounterReport("v", "n", InternalEvents.EVENT_TYPE_REGULAR.typeId)
        val environment = ReporterEnvironment(
            ProcessConfiguration(context, null),
            CounterConfiguration("api-key"),
            ErrorEnvironment(
                SimpleMapLimitation(
                    PublicLogger.getAnonymousInstance(),
                    ErrorEnvironment.TAG
                )
            ),
            null
        )

        val marshalled = marshal(write(report, environment.configBundle))
        val decoded = read(marshalled)
        val clientConfiguration = ClientConfiguration.fromBundle(context, marshalled)

        Assertions.ObjectPropertyAssertions(decoded)
            .withIgnoredFields("systemTimeProvider", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("type", InternalEvents.EVENT_TYPE_REGULAR.typeId)
            .checkField("name", "n")
            .checkField("value", "v")
            .checkField("customType", 0)
            .checkField("bytesTruncated", 0)
            .checkField("creationElapsedRealtime", CURRENT_ELAPSED_REALTIME)
            .checkField("creationTimestamp", CURRENT_TIME_MILLIS)
            .let { checkExtrasContent(it, emptyMap()) }
            .checkFieldIsNull("eventEnvironment")
            .checkFieldIsNull("profileID")
            .checkFieldIsNull("source")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("valueProtocolVersion")
            .checkAll()

        assertThat(clientConfiguration).isNotNull
        assertThat(clientConfiguration!!.reporterConfiguration.apiKey).isEqualTo("api-key")
    }

    @Test
    fun emptyBundleDeserializesAsUndefined() {
        val decoded = read(Bundle())

        Assertions.ObjectPropertyAssertions(decoded)
            .withIgnoredFields("systemTimeProvider", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("type", InternalEvents.EVENT_TYPE_UNDEFINED.typeId)
            .checkField("customType", 0)
            .checkField("value", "")
            .checkField("bytesTruncated", 0)
            .checkField("creationElapsedRealtime", 0L)
            .checkField("creationTimestamp", 0L)
            .let { checkExtrasContent(it, emptyMap()) }
            .checkFieldIsNull("name")
            .checkFieldIsNull("eventEnvironment")
            .checkFieldIsNull("profileID")
            .checkFieldIsNull("source")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("valueProtocolVersion")
            .checkAll()

        assertThat(decoded.isUndefinedType).isTrue
    }

    @Test
    fun wrongTypeValueFallsBackToUndefined() {
        val bundle = marshal(
            Bundle().apply {
                putString(EventIpcBundleKeys.TYPE, "not-int")
            }
        )

        val decoded = read(bundle)

        assertThat(decoded.type).isEqualTo(InternalEvents.EVENT_TYPE_UNDEFINED.typeId)
        assertThat(decoded.isUndefinedType).isTrue
    }

    @Test
    fun malformedPayloadTypeYieldsNullPayload() {
        val bundle = marshal(
            Bundle().apply {
                putInt(EventIpcBundleKeys.TYPE, InternalEvents.EVENT_TYPE_REGULAR.typeId)
                putString(EventIpcBundleKeys.PAYLOAD, "not-a-bundle")
            }
        )

        val decoded = read(bundle)

        Assertions.ObjectPropertyAssertions(decoded)
            .withIgnoredFields("systemTimeProvider", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("type", InternalEvents.EVENT_TYPE_REGULAR.typeId)
            .checkField("customType", 0)
            .checkField("value", "")
            .checkField("bytesTruncated", 0)
            .checkField("creationElapsedRealtime", 0L)
            .checkField("creationTimestamp", 0L)
            .let { checkExtrasContent(it, emptyMap()) }
            .checkFieldIsNull("name")
            .checkFieldIsNull("eventEnvironment")
            .checkFieldIsNull("profileID")
            .checkFieldIsNull("source")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("valueProtocolVersion")
            .checkAll()
    }

    @Test
    fun sourceAbsentMeansNull() {
        val report = CounterReport().apply {
            type = InternalEvents.EVENT_TYPE_REGULAR.typeId
            setSource(null)
            setCreationEllapsedRealtime(0L)
            setCreationTimestamp(0L)
        }

        val decoded = read(marshal(write(report)))

        Assertions.ObjectPropertyAssertions(decoded)
            .withIgnoredFields("systemTimeProvider", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("type", InternalEvents.EVENT_TYPE_REGULAR.typeId)
            .checkField("customType", 0)
            .checkField("value", StringUtils.EMPTY)
            .checkField("name", StringUtils.EMPTY)
            .checkField("bytesTruncated", 0)
            .checkField("creationElapsedRealtime", 0L)
            .checkField("creationTimestamp", 0L)
            .let { checkExtrasContent(it, emptyMap()) }
            .checkFieldIsNull("eventEnvironment")
            .checkFieldIsNull("profileID")
            .checkFieldIsNull("source")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("valueProtocolVersion")
            .checkAll()
    }

    @Test
    fun sourcePresentRoundTrips() {
        val report = CounterReport().apply {
            type = InternalEvents.EVENT_TYPE_REGULAR.typeId
            setSource(EventSource.NATIVE)
            setCreationEllapsedRealtime(0L)
            setCreationTimestamp(0L)
        }

        val decoded = read(marshal(write(report)))

        Assertions.ObjectPropertyAssertions(decoded)
            .withIgnoredFields("systemTimeProvider", "valueBytes", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("type", InternalEvents.EVENT_TYPE_REGULAR.typeId)
            .checkField("customType", 0)
            .checkField("value", StringUtils.EMPTY)
            .checkField("name", StringUtils.EMPTY)
            .checkField("bytesTruncated", 0)
            .checkField("creationElapsedRealtime", 0L)
            .checkField("creationTimestamp", 0L)
            .checkField("source", EventSource.NATIVE)
            .let { checkExtrasContent(it, emptyMap()) }
            .checkFieldIsNull("eventEnvironment")
            .checkFieldIsNull("profileID")
            .checkFieldIsNull("payload")
            .checkFieldIsNull("valueProtocolVersion")
            .checkAll()
    }

    private companion object {
        private const val CURRENT_TIME_MILLIS = 100_500L
        private const val CURRENT_ELAPSED_REALTIME = 200_500L
    }
}

package io.appmetrica.analytics.impl.preparer

import android.content.ContentValues
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.EventSource
import io.appmetrica.analytics.impl.FirstOccurrenceStatus
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.db.event.DbLocationModel
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage
import io.appmetrica.analytics.impl.protobuf.client.EventExtrasProto.EventExtras
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.nullable
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventPreparerTest : CommonTest() {

    private val name = "name"
    private val value = "value"
    private val environment = "event environment"
    private val type = InternalEvents.EVENT_TYPE_ALIVE
    private val numberInSession = 6L
    private val globalNumber = 99L
    private val numberOfType = 12L
    private val time = 24761523L
    private val bytesTruncated = 144
    private val encryptionMode = EventEncryptionMode.AES_VALUE_ENCRYPTION
    private val profileId = "profile id"
    private val firstOccurrenceStatus = FirstOccurrenceStatus.FIRST_OCCURRENCE
    private val inputLocation: DbLocationModel = mock()
    private val connectionType = 12
    private val cellularConnectionType = "cellular connection type"
    private val source = EventSource.JS
    private val attributionIdChanged = true
    private val openId = 8000
    private val extras = byteArrayOf(1, 3, 5, 7, 9)

    @get:Rule
    val rule = GlobalServiceLocatorRule()

    private val nameComposer: NameComposer = mock()
    private val valueComposer: ValueComposer = mock()
    private val eventTypeComposer: EventTypeComposer = mock()
    private val locationInfoComposer: LocationInfoComposer = mock()
    private val networkInfoComposer: NetworkInfoComposer = mock()
    private val extrasComposer: ExtrasComposer = mock()
    private val encodingTypeProvider: EncodingTypeProvider = mock()
    private val mConfig: ReportRequestConfig = mock()
    private val location: ReportMessage.Location = mock()
    private val networkInfo: ReportMessage.Session.Event.NetworkInfo = mock()
    private val eventFromDbModel: EventFromDbModel = mock()

    private var eventPreparer = EventPreparer.builderWithDefaults()
        .withNameComposer(nameComposer)
        .withValueComposer(valueComposer)
        .withEncodingTypeProvider(encodingTypeProvider)
        .withEventTypeComposer(eventTypeComposer)
        .withLocationInfoComposer(locationInfoComposer)
        .withNetworkInfoComposer(networkInfoComposer)
        .withExtrasComposer(extrasComposer)
        .build()

    @Before
    fun setUp() {
        whenever(eventFromDbModel.name).thenReturn(name)
        whenever(eventFromDbModel.value).thenReturn(value)
        whenever(eventFromDbModel.index).thenReturn(numberInSession)
        whenever(eventFromDbModel.globalNumber).thenReturn(globalNumber)
        whenever(eventFromDbModel.numberOfType).thenReturn(numberOfType)
        whenever(eventFromDbModel.time).thenReturn(time)
        whenever(eventFromDbModel.locationData).thenReturn(inputLocation)
        whenever(eventFromDbModel.eventType).thenReturn(type)
        whenever(eventFromDbModel.eventEnvironment).thenReturn(environment)
        whenever(eventFromDbModel.bytesTruncated).thenReturn(bytesTruncated)
        whenever(eventFromDbModel.connectionType).thenReturn(connectionType)
        whenever(eventFromDbModel.cellularConnectionType).thenReturn(cellularConnectionType)
        whenever(eventFromDbModel.profileID).thenReturn(profileId)
        whenever(eventFromDbModel.eventEncryptionMode).thenReturn(encryptionMode)
        whenever(eventFromDbModel.firstOccurrenceStatus).thenReturn(firstOccurrenceStatus)
        whenever(eventFromDbModel.source).thenReturn(source)
        whenever(eventFromDbModel.attributionIdChanged).thenReturn(attributionIdChanged)
        whenever(eventFromDbModel.openId).thenReturn(openId)
        whenever(eventFromDbModel.extras).thenReturn(extras)
    }

    @Test
    fun defaultPreparer() {
        ObjectPropertyAssertions(EventPreparer.defaultPreparer())
            .withPrivateFields(true)
            .checkFieldIsInstanceOf("mNameComposer", SameNameComposer::class.java)
            .checkFieldIsInstanceOf("mValueComposer", StringValueComposer::class.java)
            .checkFieldIsInstanceOf("mEncodingTypeProvider", NoneEncodingTypeProvider::class.java)
            .checkFieldIsInstanceOf("mEventTypeComposer", SameEventTypeComposer::class.java)
            .checkFieldIsInstanceOf("locationInfoComposer", FullLocationInfoComposer::class.java)
            .checkFieldIsInstanceOf("networkInfoComposer", FullNetworkInfoComposer::class.java)
            .checkFieldIsInstanceOf("extrasComposer", FullExtrasComposer::class.java)
            .checkAll()
    }

    @Test
    fun toProto() {
        val composedName = "composed name"
        val composedValue = "composed value"
        val composedType = 55
        val providedEncryptionMode = 1
        val extrasEntry: ReportMessage.Session.Event.ExtrasEntry = mock()
        val extrasProto = arrayOf(extrasEntry)

        whenever(nameComposer.getName(name)).thenReturn(composedName)
        whenever(valueComposer.getValue(eventFromDbModel, mConfig)).thenReturn(composedValue.toByteArray())
        whenever(encodingTypeProvider.getEncodingType(encryptionMode)).thenReturn(providedEncryptionMode)
        whenever(eventTypeComposer.getEventType(eventFromDbModel)).thenReturn(composedType)
        whenever(locationInfoComposer.getLocation(inputLocation)).thenReturn(location)
        whenever(networkInfoComposer.getNetworkInfo(connectionType, cellularConnectionType)).thenReturn(networkInfo)
        whenever(extrasComposer.getExtras(extras)).thenReturn(extrasProto)

        val proto = eventPreparer.toSessionEvent(eventFromDbModel, mConfig)

        ObjectPropertyAssertions(proto)
            .withFinalFieldOnly(false)
            .withIgnoredFields(
                "account",
                "locationTrackingEnabled",
            )
            .checkField("name", composedName)
            .checkField("value", composedValue.toByteArray())
            .checkField("environment", environment)
            .checkField("type", composedType)
            .checkField("numberInSession", numberInSession)
            .checkField("globalNumber", globalNumber.toLong())
            .checkField("numberOfType", numberOfType)
            .checkField("time", time)
            .checkField("bytesTruncated", bytesTruncated)
            .checkField("encodingType", providedEncryptionMode)
            .checkField("profileId", profileId.toByteArray())
            .checkField("firstOccurrence", ReportMessage.OPTIONAL_BOOL_TRUE)
            .checkField("source", source.code)
            .checkField("location", location)
            .checkField("networkInfo", networkInfo)
            .checkField("attributionIdChanged", attributionIdChanged)
            .checkField("openId", openId.toLong())
            .checkField("extras", extrasProto)
            .checkAll()
    }

    @Test
    fun toProtoNullables() {
        val event = EventFromDbModel(ContentValues())
        val composedType = 1

        whenever(nameComposer.getName(nullable(String::class.java))).thenReturn(null)
        whenever(valueComposer.getValue(event, mConfig)).thenReturn(ByteArray(0))
        whenever(encodingTypeProvider.getEncodingType(any())).thenReturn(ReportMessage.Session.Event.NONE)
        whenever(eventTypeComposer.getEventType(event)).thenReturn(composedType)
        whenever(extrasComposer.getExtras(null)).thenReturn(ReportMessage.Session.Event.ExtrasEntry.emptyArray())

        val proto = eventPreparer.toSessionEvent(event, mConfig)

        ObjectPropertyAssertions(proto)
            .withFinalFieldOnly(false)
            .withIgnoredFields(
                "networkInfo",
                "location",
                "account",
                "locationTrackingEnabled",
            )
            .checkField("name", "")
            .checkField("value", ByteArray(0))
            .checkField("environment", "")
            .checkField("type", composedType)
            .checkField("numberInSession", 0L)
            .checkField("globalNumber", 0L)
            .checkField("numberOfType", 0L)
            .checkField("time", 0L)
            .checkField("bytesTruncated", 0)
            .checkField("encodingType", ReportMessage.Session.Event.NONE)
            .checkField("profileId", ByteArray(0))
            .checkField("firstOccurrence", ReportMessage.OPTIONAL_BOOL_UNDEFINED)
            .checkField("source", ReportMessage.Session.Event.NATIVE)
            .checkField("attributionIdChanged", false)
            .checkField("openId", 1L)
            .checkField("extras", arrayOfNulls<EventExtras.ExtrasEntry>(0))
            .checkAll()
    }

    @Test
    fun toProtoFirstOccurrenceNonFirst() {
        val cv = ContentValues()
        val proto = eventPreparer.toSessionEvent(EventFromDbModel(cv), mConfig)

        assertThat(proto.firstOccurrence)
            .isEqualTo(ReportMessage.OPTIONAL_BOOL_UNDEFINED)
    }

    @Test
    fun locationEnabledInConfig() {
        val location = DbLocationModel(
            enabled = true,
            latitude = 1.0,
            longitude = 1.0,
            timestamp = null,
            precision = null,
            direction = null,
            speed = null,
            altitude = null,
            provider = null,
            originalProvider = null,
        )

        assertThat(eventPreparer.getTrackLocationEnabled(location)).isEqualTo(ReportMessage.OPTIONAL_BOOL_TRUE)
    }

    @Test
    fun locationDisabledInConfig() {
        val location = DbLocationModel(
            enabled = false,
            latitude = 1.0,
            longitude = 1.0,
            timestamp = null,
            precision = null,
            direction = null,
            speed = null,
            altitude = null,
            provider = null,
            originalProvider = null,
        )

        assertThat(eventPreparer.getTrackLocationEnabled(location)).isEqualTo(ReportMessage.OPTIONAL_BOOL_FALSE)
    }

    @Test
    fun unknownLocationEnabledState() {
        val location = DbLocationModel(
            enabled = null,
            latitude = 1.0,
            longitude = 1.0,
            timestamp = null,
            precision = null,
            direction = null,
            speed = null,
            altitude = null,
            provider = null,
            originalProvider = null,
        )

        assertThat(eventPreparer.getTrackLocationEnabled(location)).isEqualTo(ReportMessage.OPTIONAL_BOOL_UNDEFINED)
    }
}

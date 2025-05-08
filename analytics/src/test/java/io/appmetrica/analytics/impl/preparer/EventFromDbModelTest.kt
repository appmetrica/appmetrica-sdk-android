package io.appmetrica.analytics.impl.preparer

import android.content.ContentValues
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.EventSource
import io.appmetrica.analytics.impl.FirstOccurrenceStatus
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.db.event.DbEventModel
import io.appmetrica.analytics.impl.db.event.DbLocationModel
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventModelConverter
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventFromDbModelTest : CommonTest() {

    private val type = InternalEvents.EVENT_TYPE_REGULAR
    private val customType = 41
    private val name = "some name"
    private val value = "some value"
    private val time = 83274576L
    private val numberInSession = 11L
    private val globalNumber = 57L
    private val numberOfType = 42L
    private val locationInfo: DbLocationModel = mock()
    private val eventEnvironment = "event environment"
    private val truncated = 33
    private val connectionType = 5
    private val cellConnectionType = "cellular connection type"
    private val profileId = "profile id"
    private val encryptionMode = EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER
    private val firstOccurrenceStatus = FirstOccurrenceStatus.NON_FIRST_OCCURENCE
    private val source = EventSource.JS
    private val attributionIdChanged = true
    private val openId = 7777
    private val extras = byteArrayOf(1, 3, 5, 7, 9)

    private val contentValues: ContentValues = mock()
    private val model: DbEventModel = mock()
    private val modelDescription: DbEventModel.Description = mock()

    @get:Rule
    val converterRule = MockedConstructionRule(DbEventModelConverter::class.java) { mock, _ ->
        whenever(mock.toModel(contentValues)).thenReturn(model)
    }

    @Before
    fun setUp() {
        whenever(model.numberInSession).thenReturn(numberInSession)
        whenever(model.type).thenReturn(type)
        whenever(model.globalNumber).thenReturn(globalNumber)
        whenever(model.time).thenReturn(time)
        whenever(model.description).thenReturn(modelDescription)

        whenever(modelDescription.customType).thenReturn(customType)
        whenever(modelDescription.name).thenReturn(name)
        whenever(modelDescription.value).thenReturn(value)
        whenever(modelDescription.numberOfType).thenReturn(numberOfType)
        whenever(modelDescription.locationInfo).thenReturn(locationInfo)
        whenever(modelDescription.errorEnvironment).thenReturn(eventEnvironment)
        whenever(modelDescription.truncated).thenReturn(truncated)
        whenever(modelDescription.connectionType).thenReturn(connectionType)
        whenever(modelDescription.cellularConnectionType).thenReturn(cellConnectionType)
        whenever(modelDescription.encryptingMode).thenReturn(encryptionMode)
        whenever(modelDescription.profileId).thenReturn(profileId)
        whenever(modelDescription.firstOccurrenceStatus).thenReturn(firstOccurrenceStatus)
        whenever(modelDescription.source).thenReturn(source)
        whenever(modelDescription.attributionIdChanged).thenReturn(attributionIdChanged)
        whenever(modelDescription.openId).thenReturn(openId)
        whenever(modelDescription.extras).thenReturn(extras)
    }

    @Test
    fun constructor() {
        val event = EventFromDbModel(contentValues)

        ObjectPropertyAssertions(event)
            .withFinalFieldOnly(false)
            .withIgnoredFields("dbEventModel")
            .checkField("name", name)
            .checkField("value", value)
            .checkField("index", numberInSession)
            .checkField("globalNumber", globalNumber)
            .checkField("numberOfType", numberOfType)
            .checkField("time", time)
            .checkField("locationData", locationInfo)
            .checkField("eventType", type)
            .checkField("customType", customType)
            .checkField("eventEnvironment", eventEnvironment)
            .checkField("bytesTruncated", truncated)
            .checkField("connectionType", connectionType)
            .checkField("cellularConnectionType", cellConnectionType)
            .checkField("profileID", profileId)
            .checkField("eventEncryptionMode", encryptionMode)
            .checkField("firstOccurrenceStatus", firstOccurrenceStatus)
            .checkField("source", source)
            .checkField("attributionIdChanged", attributionIdChanged)
            .checkField("openId", openId)
            .checkField("extras", extras)
            .checkAll()
    }

    @Test
    fun constructorIfNoEncryptingMode() {
        whenever(modelDescription.encryptingMode).thenReturn(null)

        val event = EventFromDbModel(contentValues)

        assertThat(event.eventEncryptionMode).isEqualTo(EventEncryptionMode.NONE)
    }

    @Test
    fun constructorIfNoFirstOccurrenceStatus() {
        whenever(modelDescription.firstOccurrenceStatus).thenReturn(null)

        val event = EventFromDbModel(contentValues)

        assertThat(event.firstOccurrenceStatus).isEqualTo(FirstOccurrenceStatus.UNKNOWN)
    }

    @Test
    fun updateValue() {
        val value = "new value"
        val event = EventFromDbModel(contentValues)
        event.updateValue(value)

        assertThat(event.value).isEqualTo(value)
    }
}

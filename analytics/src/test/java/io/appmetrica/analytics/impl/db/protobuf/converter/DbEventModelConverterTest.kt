package io.appmetrica.analytics.impl.db.protobuf.converter

import android.content.ContentValues
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.event.DbEventModel
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class DbEventModelConverterTest : CommonTest() {

    private val session = 424242L
    private val sessionType = SessionType.FOREGROUND
    private val numberInSession = 100500L
    private val type = InternalEvents.EVENT_TYPE_ACTIVATION
    private val globalNumber = 1234L
    private val time = 123123L

    private val description: DbEventModel.Description = mock()
    private val descriptionBytes = "descriptionBytes".toByteArray()

    private val descriptionConverter: DbEventDescriptionToBytesConverter = mock()
    private val converter = DbEventModelConverter(descriptionConverter)

    @Before
    fun setUp() {
        whenever(descriptionConverter.fromModel(description)).thenReturn(descriptionBytes)
        whenever(descriptionConverter.toModel(descriptionBytes)).thenReturn(description)
    }

    @Test
    fun fromModel() {
        val model = DbEventModel(
            session,
            sessionType,
            numberInSession,
            type,
            globalNumber,
            time,
            description
        )
        val cv = converter.fromModel(model)
        SoftAssertions().apply {
            assertThat(cv.getAsLong(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION))
                .isEqualTo(session)
            assertThat(cv.getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE))
                .isEqualTo(sessionType.code)
            assertThat(cv.getAsLong(Constants.EventsTable.EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION))
                .isEqualTo(numberInSession)
            assertThat(cv.getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE))
                .isEqualTo(type.typeId)
            assertThat(cv.getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER))
                .isEqualTo(globalNumber)
            assertThat(cv.getAsLong(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TIME))
                .isEqualTo(time)
            assertThat(cv.getAsByteArray(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION))
                .isEqualTo(descriptionBytes)
        }.assertAll()
    }

    @Test
    fun fromModelIfNullFields() {
        val model = DbEventModel(
            null,
            null,
            null,
            null,
            null,
            null,
            description
        )
        val cv = converter.fromModel(model)
        SoftAssertions().apply {
            assertThat(cv.containsKey(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION))
                .isFalse
            assertThat(cv.containsKey(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE))
                .isFalse
            assertThat(cv.containsKey(Constants.EventsTable.EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION))
                .isFalse
            assertThat(cv.containsKey(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE))
                .isFalse
            assertThat(cv.containsKey(Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER))
                .isFalse
            assertThat(cv.containsKey(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TIME))
                .isFalse
            assertThat(cv.getAsByteArray(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION))
                .isEqualTo(descriptionBytes)
        }.assertAll()
    }

    @Test
    fun toModel() {
        val cv = ContentValues().apply {
            put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION, session)
            put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE, sessionType.code)
            put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION, numberInSession)
            put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE, type.typeId)
            put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER, globalNumber)
            put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TIME, time)
            put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, descriptionBytes)
        }
        val model = converter.toModel(cv)
        SoftAssertions().apply {
            assertThat(model.session).isEqualTo(session)
            assertThat(model.sessionType).isEqualTo(sessionType)
            assertThat(model.numberInSession).isEqualTo(numberInSession)
            assertThat(model.type).isEqualTo(type)
            assertThat(model.globalNumber).isEqualTo(globalNumber)
            assertThat(model.time).isEqualTo(time)
            assertThat(model.description).isEqualTo(description)
        }.assertAll()
    }

    @Test
    fun toModelIfNullFields() {
        val cv = ContentValues().apply {
            put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION, descriptionBytes)
        }
        val model = converter.toModel(cv)
        SoftAssertions().apply {
            assertThat(model.session).isNull()
            assertThat(model.sessionType).isNull()
            assertThat(model.numberInSession).isNull()
            assertThat(model.type).isNull()
            assertThat(model.globalNumber).isNull()
            assertThat(model.time).isNull()
            assertThat(model.description).isEqualTo(description)
        }.assertAll()
    }

    @Test
    fun fromAndToModel() {
        val model = DbEventModel(
            session,
            sessionType,
            numberInSession,
            type,
            globalNumber,
            time,
            description
        )
        val cv = converter.fromModel(model)
        val rebuildModel = converter.toModel(cv)
        assertThat(rebuildModel).isEqualToComparingFieldByField(model)
    }

    @Test
    fun fromAndToModelIfNullFields() {
        val model = DbEventModel(
            null,
            null,
            null,
            null,
            null,
            null,
            description
        )
        val cv = converter.fromModel(model)
        val rebuildModel = converter.toModel(cv)
        assertThat(rebuildModel).isEqualToComparingFieldByField(model)
    }
}

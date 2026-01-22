package io.appmetrica.analytics.impl.db.protobuf.converter

import android.content.ContentValues
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.session.DbSessionModel
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
internal class DbSessionModelConverterTest : CommonTest() {

    private val id = 424242L
    private val type = SessionType.FOREGROUND
    private val reportRequestParameters = "some string"
    private val description: DbSessionModel.Description = mock()
    private val descriptionBytes = "descriptionBytes".toByteArray()

    private val dbSessionDescriptionToBytesConverter: DbSessionDescriptionToBytesConverter = mock()
    private val converter = DbSessionModelConverter(dbSessionDescriptionToBytesConverter)

    @Before
    fun setUp() {
        whenever(dbSessionDescriptionToBytesConverter.fromModel(description)).thenReturn(descriptionBytes)
        whenever(dbSessionDescriptionToBytesConverter.toModel(descriptionBytes)).thenReturn(description)
    }

    @Test
    fun fromModel() {
        val model = DbSessionModel(
            id,
            type,
            reportRequestParameters,
            description
        )
        val cv = converter.fromModel(model)
        SoftAssertions().apply {
            assertThat(cv.getAsLong(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_ID))
                .isEqualTo(id)
            assertThat(cv.getAsInteger(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_TYPE))
                .isEqualTo(type.code)
            assertThat(cv.getAsString(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS))
                .isEqualTo(reportRequestParameters)
            assertThat(cv.getAsByteArray(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_DESCRIPTION))
                .isEqualTo(descriptionBytes)
        }.assertAll()
    }

    @Test
    fun fromModelIfNullFields() {
        val model = DbSessionModel(
            null,
            null,
            null,
            description
        )
        val cv = converter.fromModel(model)
        SoftAssertions().apply {
            assertThat(cv.containsKey(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_ID))
                .isFalse
            assertThat(cv.containsKey(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_TYPE))
                .isFalse
            assertThat(cv.containsKey(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS))
                .isFalse
            assertThat(cv.getAsByteArray(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_DESCRIPTION))
                .isEqualTo(descriptionBytes)
        }.assertAll()
    }

    @Test
    fun toModel() {
        val cv = ContentValues().apply {
            put(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_ID, id)
            put(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_TYPE, type.code)
            put(
                Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS,
                reportRequestParameters
            )
            put(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_DESCRIPTION, descriptionBytes)
        }
        val model = converter.toModel(cv)
        SoftAssertions().apply {
            assertThat(model.id).isEqualTo(id)
            assertThat(model.type).isEqualTo(type)
            assertThat(model.reportRequestParameters).isEqualTo(reportRequestParameters)
            assertThat(model.description).isEqualTo(description)
        }.assertAll()
    }

    @Test
    fun toModelIfNullFields() {
        val cv = ContentValues().apply {
            put(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_DESCRIPTION, descriptionBytes)
        }
        val model = converter.toModel(cv)
        SoftAssertions().apply {
            assertThat(model.id).isNull()
            assertThat(model.type).isNull()
            assertThat(model.reportRequestParameters).isNull()
            assertThat(model.description).isEqualTo(description)
        }.assertAll()
    }

    @Test
    fun fromAndToModel() {
        val model = DbSessionModel(
            id,
            type,
            reportRequestParameters,
            description
        )
        val cv = converter.fromModel(model)
        val rebuildModel = converter.toModel(cv)
        assertThat(rebuildModel).isEqualToComparingFieldByField(model)
    }

    @Test
    fun fromAndToModelIfNullFields() {
        val model = DbSessionModel(
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

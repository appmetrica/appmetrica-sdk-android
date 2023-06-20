package io.appmetrica.analytics.impl.db.protobuf.converter

import android.content.ContentValues
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.session.DbSessionModel

class DbSessionModelConverter(
    private val descriptionConverter: DbSessionDescriptionToBytesConverter = DbSessionDescriptionToBytesConverter()
) : Converter<DbSessionModel, ContentValues> {

    override fun fromModel(value: DbSessionModel): ContentValues {
        return ContentValues().apply {
            value.id?.let {
                put(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_ID, it)
            }
            value.type?.code?.let {
                put(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_TYPE, it)
            }
            value.reportRequestParameters?.let {
                put(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS, it)
            }
            put(
                Constants.SessionTable.SessionTableEntry.FIELD_SESSION_DESCRIPTION,
                descriptionConverter.fromModel(value.description)
            )
        }
    }

    override fun toModel(value: ContentValues): DbSessionModel {
        return DbSessionModel(
            value.getAsLong(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_ID),
            value.getAsInteger(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_TYPE)
                ?.let { SessionType.getByCode(it) },
            value.getAsString(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS),
            descriptionConverter.toModel(
                value.getAsByteArray(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_DESCRIPTION)
            )
        )
    }
}

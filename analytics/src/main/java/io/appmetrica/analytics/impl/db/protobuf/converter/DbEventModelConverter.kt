package io.appmetrica.analytics.impl.db.protobuf.converter

import android.content.ContentValues
import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.event.DbEventModel

class DbEventModelConverter(
    private val descriptionConverter: DbEventDescriptionToBytesConverter = DbEventDescriptionToBytesConverter()
) : Converter<DbEventModel, ContentValues> {

    override fun fromModel(value: DbEventModel): ContentValues {
        return ContentValues().apply {
            value.session?.let {
                put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION, it)
            }
            value.sessionType?.code?.let {
                put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE, it)
            }
            value.numberInSession?.let {
                put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION, it)
            }
            value.type?.typeId?.let {
                put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE, it)
            }
            value.globalNumber?.let {
                put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER, it)
            }
            value.time?.let {
                put(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TIME, it)
            }
            put(
                Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION,
                descriptionConverter.fromModel(value.description)
            )
        }
    }

    override fun toModel(value: ContentValues): DbEventModel {
        return DbEventModel(
            value.getAsLong(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION),
            value.getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_SESSION_TYPE)
                ?.let { SessionType.getByCode(it) },
            value.getAsLong(Constants.EventsTable.EventTableEntry.FIELD_EVENT_NUMBER_IN_SESSION),
            value.getAsInteger(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TYPE)
                ?.let { InternalEvents.valueOf(it) },
            value.getAsLong(Constants.EventsTable.EventTableEntry.FIELD_EVENT_GLOBAL_NUMBER),
            value.getAsLong(Constants.EventsTable.EventTableEntry.FIELD_EVENT_TIME),
            descriptionConverter.toModel(
                value.getAsByteArray(Constants.EventsTable.EventTableEntry.FIELD_EVENT_DESCRIPTION)
            )
        )
    }
}

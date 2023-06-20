package io.appmetrica.analytics.impl.preparer

import android.content.ContentValues
import io.appmetrica.analytics.impl.EventSource
import io.appmetrica.analytics.impl.FirstOccurrenceStatus
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.db.event.DbLocationModel
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventModelConverter
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode

class EventFromDbModel(cv: ContentValues) {

    private val dbEventModel = DbEventModelConverter().toModel(cv)

    val name: String? = dbEventModel.description.name
    var value: String? = dbEventModel.description.value
    val index: Long? = dbEventModel.numberInSession
    val globalNumber: Long? = dbEventModel.globalNumber
    val numberOfType: Long? = dbEventModel.description.numberOfType
    val time: Long? = dbEventModel.time
    val locationData: DbLocationModel? = dbEventModel.description.locationInfo
    val eventType: InternalEvents? = dbEventModel.type
    val customType: Int? = dbEventModel.description.customType
    val eventEnvironment: String? = dbEventModel.description.errorEnvironment
    val bytesTruncated: Int? = dbEventModel.description.truncated
    val connectionType: Int? = dbEventModel.description.connectionType
    val cellularConnectionType: String? = dbEventModel.description.cellularConnectionType
    val profileID: String? = dbEventModel.description.profileId
    val eventEncryptionMode: EventEncryptionMode = dbEventModel.description.encryptingMode
        ?: EventEncryptionMode.valueOf(null as Int?)
    val firstOccurrenceStatus: FirstOccurrenceStatus = dbEventModel.description.firstOccurrenceStatus
        ?: FirstOccurrenceStatus.fromStatusCode(null)
    val source: EventSource? = dbEventModel.description.source
    val attributionIdChanged: Boolean? = dbEventModel.description.attributionIdChanged
    val openId: Int? = dbEventModel.description.openId
    val extras: ByteArray? = dbEventModel.description.extras

    fun updateValue(newValue: String?) {
        value = newValue
    }
}

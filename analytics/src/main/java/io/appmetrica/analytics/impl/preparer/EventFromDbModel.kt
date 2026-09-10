package io.appmetrica.analytics.impl.preparer

import android.content.ContentValues
import io.appmetrica.analytics.impl.AppEnvironment
import io.appmetrica.analytics.impl.EventSource
import io.appmetrica.analytics.impl.FirstOccurrenceStatus
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.db.event.DbLocationModel
import io.appmetrica.analytics.impl.db.protobuf.converter.DbEventModelConverter

internal class EventFromDbModel(cv: ContentValues) {

    private val dbEventModel = DbEventModelConverter().toModel(cv)

    val name: String? = dbEventModel.description.name

    /**
     * Present when the event was stored in deprecated string field 3.
     * Cleared after [updateValue].
     */
    private var legacyStringValue: String? = dbEventModel.description.value

    /**
     * Canonical payload from [DbEventModel.Description.valueBytes],
     * or UTF-8 bytes written via [updateValue].
     */
    private var valueBytes: ByteArray? = dbEventModel.description.valueBytes

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
    val firstOccurrenceStatus: FirstOccurrenceStatus = dbEventModel.description.firstOccurrenceStatus
        ?: FirstOccurrenceStatus.fromStatusCode(null)
    val source: EventSource? = dbEventModel.description.source
    val attributionIdChanged: Boolean? = dbEventModel.description.attributionIdChanged
    val openId: Int? = dbEventModel.description.openId
    val extras: ByteArray? = dbEventModel.description.extras
    val appEnvironment: String = dbEventModel.description.appEnvironment
        ?: AppEnvironment.DEFAULT_ENVIRONMENT_JSON_STRING
    val appEnvironmentRevision: Long = dbEventModel.description.appEnvironmentRevision ?: 0L
    val valueProtocolVersion: Int? = dbEventModel.description.valueProtocolVersion

    /**
     * Legacy-first dual-read: prefers deprecated string field, else [valueBytes], else empty.
     */
    fun <T> foldValue(
        onLegacy: (String) -> T,
        onBytes: (ByteArray) -> T,
        onEmpty: () -> T,
    ): T {
        legacyStringValue?.let { return onLegacy(it) }
        valueBytes?.let { return onBytes(it) }
        return onEmpty()
    }

    fun updateValue(newValue: String?) {
        legacyStringValue = null
        valueBytes = newValue?.toByteArray(Charsets.UTF_8)
    }
}

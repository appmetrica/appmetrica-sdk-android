package io.appmetrica.analytics.impl.db.event

import io.appmetrica.analytics.impl.EventSource
import io.appmetrica.analytics.impl.FirstOccurrenceStatus
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode

internal class DbEventModel(
    val session: Long?,
    val sessionType: SessionType?,
    val numberInSession: Long?,
    val type: InternalEvents?,
    val globalNumber: Long?,
    val time: Long?,
    val description: Description
) {

    internal class Description(
        val customType: Int?,
        val name: String?,
        val value: String?,
        val numberOfType: Long?,
        val locationInfo: DbLocationModel?,
        val errorEnvironment: String?,
        val appEnvironment: String?,
        val appEnvironmentRevision: Long?,
        val truncated: Int?,
        val connectionType: Int?,
        val cellularConnectionType: String?,
        val encryptingMode: EventEncryptionMode?,
        val profileId: String?,
        val firstOccurrenceStatus: FirstOccurrenceStatus?,
        val source: EventSource?,
        val attributionIdChanged: Boolean?,
        val openId: Int?,
        val extras: ByteArray?
    )
}

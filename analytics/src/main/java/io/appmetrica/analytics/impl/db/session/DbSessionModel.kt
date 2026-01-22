package io.appmetrica.analytics.impl.db.session

import io.appmetrica.analytics.impl.component.session.SessionType

internal class DbSessionModel(
    val id: Long?,
    val type: SessionType?,
    val reportRequestParameters: String?,
    val description: Description,
) {

    internal class Description(
        val startTime: Long?,
        val serverTimeOffset: Long?,
        val obtainedBeforeFirstSynchronization: Boolean?
    )
}

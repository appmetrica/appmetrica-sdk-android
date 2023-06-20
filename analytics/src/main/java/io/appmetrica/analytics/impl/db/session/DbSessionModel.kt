package io.appmetrica.analytics.impl.db.session

import io.appmetrica.analytics.impl.component.session.SessionType

class DbSessionModel(
    val id: Long?,
    val type: SessionType?,
    val reportRequestParameters: String?,
    val description: Description,
) {

    class Description(
        val startTime: Long?,
        val serverTimeOffset: Long?,
        val obtainedBeforeFirstSynchronization: Boolean?
    )
}
